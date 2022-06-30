package com.belcobtm.presentation.screens.deals.atm.sell

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.transaction.interactor.SellUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.livedata.DoubleCombinedLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.formatter.Formatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class AtmSellViewModel(
    private val getCoinListUseCase: GetCoinListUseCase,
    private val sellUseCase: SellUseCase,
    private val accountDao: AccountDao,
    private val serviceInfoProvider: ServiceInfoProvider,
    private val stringProvider: StringProvider,
    private val priceFormatter: Formatter<Double>
) : ViewModel() {

    val originCoinsData = mutableListOf<CoinDataItem>()

    private val _initLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initLoadingData: LiveData<LoadingData<Unit>> = _initLoadingData

    private val _sellLoadingData = MutableLiveData<LoadingData<Unit>>()
    val sellLoadingData: LiveData<LoadingData<Unit>> = _sellLoadingData

    private val _usdAmount = MutableLiveData(0)
    val usdAmount: LiveData<Int> = _usdAmount

    private val _usdAmountError = MutableLiveData<String?>()
    val usdAmountError: LiveData<String?> = _usdAmountError

    private val _selectedCoinModel = MutableLiveData<AtmSellCoinPresentationModel>()
    val selectedCoinModel: LiveData<AtmSellCoinPresentationModel> = _selectedCoinModel

    private val _selectedCoin = MutableLiveData<CoinDataItem>()
    val selectedCoin: LiveData<CoinDataItem> = _selectedCoin

    private val _rate = MutableLiveData<AtmSellRateModelView>()
    val rate: LiveData<AtmSellRateModelView> = _rate

    private val coinAmount: LiveData<Double> =
        DoubleCombinedLiveData(usdAmount, selectedCoin) { amount, coin ->
            val feePercent = serviceInfoProvider.getService(ServiceType.ATM_SELL)?.feePercent ?: 0.0
            val price = coin?.priceUsd ?: 0.0
            (amount?.toDouble() ?: 0.0) / price * (100 + feePercent) / 100.0
        }

    val formattedCoinAmount: LiveData<String> =
        DoubleCombinedLiveData(coinAmount, selectedCoin) { amount, coin ->
            "${amount?.toStringCoin().orEmpty()} ${coin?.code.orEmpty()}"
        }

    val fee: LiveData<AtmSellFeeModelView> =
        DoubleCombinedLiveData(usdAmount, selectedCoin) { amount, coin ->
            val feePercent = serviceInfoProvider.getService(ServiceType.ATM_SELL)?.feePercent ?: 0.0
            val price = coin?.priceUsd ?: 0.0
            AtmSellFeeModelView(
                feePercent,
                (amount?.toDouble() ?: 0.0) / price * (feePercent / 100.0),
                coin?.code.orEmpty()
            )
        }

    private val _todayLimit = MutableLiveData<Double>()
    private val _txLimit = MutableLiveData<Double>()

    private val _todayLimitFormatted = MutableLiveData<String>()
    val todayLimitFormatted: LiveData<String> = _todayLimitFormatted

    private val _txLimitFormatted = MutableLiveData<String>()
    val txLimitFormatted: LiveData<String> = _txLimitFormatted

    private val _dailyLimitFormatted = MutableLiveData<String>()
    val dailyLimitFormatted: LiveData<String> = _dailyLimitFormatted

    fun loadInitialData() {
        viewModelScope.launch {
            _initLoadingData.value = LoadingData.Loading()
            val allCoins = withContext(Dispatchers.IO) {
                accountDao.getItemList().orEmpty()
                    .filter(AccountEntity::isEnabled)
                    .associateBy { it.type.name }
            }
            getCoinListUseCase(
                params = Unit,
                onSuccess = { coinsDataList ->
                    originCoinsData.clear()
                    originCoinsData.addAll(coinsDataList.filter { allCoins[it.code] != null })
                    if (originCoinsData.isNotEmpty()) {
                        loadLimits(originCoinsData[0])
                    } else {
                        _initLoadingData.value =
                            LoadingData.Error(Failure.OperationCannotBePerformed)
                    }
                },
                onError = { _initLoadingData.value = LoadingData.Error(it) }
            )
        }
    }

    fun setMaxSendAmount() {
        val currentCoinToSend = selectedCoin.value ?: return
        val a = (currentCoinToSend.reservedBalanceCoin * currentCoinToSend.priceUsd *
            (100.toDouble() - (serviceInfoProvider.getService(ServiceType.ATM_SELL)?.feePercent
                ?: 0.0)) / 100.0).toInt()
        val maxAmount = max(a / 20 * 20, max(a / 50 * 50, a / 100 * 100))
        setAmount(maxAmount)
    }

    fun setCoin(coin: CoinDataItem) {
        if (coin != selectedCoin.value) {
            updateCoin(coin)
        }
    }

    fun setAmount(sendAmount: Int) {
        _usdAmount.value = sendAmount
        _usdAmountError.value = null
    }

    fun sell() {
        val coin = _selectedCoin.value ?: return
        val amount = (coinAmount.value ?: return).toStringCoin().toDouble()
        val usdAmount = usdAmount.value ?: return
        val todayLimit = _todayLimit.value ?: return
        val txLimit = _txLimit.value ?: return
        val fee = serviceInfoProvider.getService(ServiceType.ATM_SELL)?.feePercent ?: 0.0
        if (usdAmount <= 0) {
            _usdAmountError.value = stringProvider.getString(R.string.sell_amount_zero)
            return
        }
        if (usdAmount % 50 != 0 && usdAmount % 20 != 0 && usdAmount % 100 != 0) {
            _usdAmountError.value = stringProvider.getString(R.string.sell_amount_wrong_divider)
            return
        }
        if (amount > coin.reservedBalanceCoin) {
            _usdAmountError.value = stringProvider.getString(R.string.sell_amount_exceeds_limit)
            return
        }
        if (txLimit < amount || todayLimit < amount) {
            _usdAmountError.value =
                stringProvider.getString(R.string.limits_exceeded_validation_message)
            return
        }
        _sellLoadingData.value = LoadingData.Loading()
        sellUseCase(
            SellUseCase.Params(coin.code, coin.priceUsd, amount, usdAmount, fee),
            onSuccess = { _sellLoadingData.value = LoadingData.Success(Unit) },
            onError = { _sellLoadingData.value = LoadingData.Error(it) }
        )
    }

    private fun updateCoin(coin: CoinDataItem) {
        _selectedCoin.value = coin
        _rate.value = AtmSellRateModelView(
            1.0, coin.code, priceFormatter.format(coin.priceUsd)
        )
        _selectedCoinModel.value = AtmSellCoinPresentationModel(
            coin.code, coin.reservedBalanceCoin, priceFormatter.format(coin.reservedBalanceUsd), 0.0
        )
    }

    private fun loadLimits(coinDataItem: CoinDataItem) {
        updateCoin(coinDataItem)
        serviceInfoProvider.getService(ServiceType.ATM_SELL)?.let {
            _todayLimit.value = it.remainLimit
            _txLimit.value = it.txLimit
            _txLimitFormatted.value = priceFormatter.format(it.txLimit)
            _dailyLimitFormatted.value = priceFormatter.format(it.dailyLimit)
            _todayLimitFormatted.value = priceFormatter.format(it.remainLimit)
            _usdAmount.value = 0
        }
        _initLoadingData.value = LoadingData.Success(Unit)
    }

}

data class AtmSellFeeModelView(
    val platformFeePercent: Double,
    val platformFeeCoinAmount: Double,
    val swapCoinCode: String
)

data class AtmSellRateModelView(
    val coinAmount: Double,
    val coinCode: String,
    val usdAmount: String
)

data class AtmSellCoinPresentationModel(
    val coinCode: String,
    val coinBalance: Double,
    val usdBalance: String,
    val coinFee: Double
)
