package com.belcobtm.presentation.features.deals.atm.sell

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
import com.belcobtm.domain.transaction.interactor.SellGetLimitsUseCase
import com.belcobtm.domain.transaction.interactor.SellUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.coin.CoinLimitsValueProvider
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.livedata.DoubleCombinedLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.features.deals.swap.CoinPresentationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AtmSellViewModel(
    private val getCoinListUseCase: GetCoinListUseCase,
    private val sellGetLimitsUseCase: SellGetLimitsUseCase,
    private val sellUseCase: SellUseCase,
    private val accountDao: AccountDao,
    private val coinLimitsValueProvider: CoinLimitsValueProvider,
    private val serviceInfoProvider: ServiceInfoProvider,
    private val stringProvider: StringProvider,
    private val priceFormatter: Formatter<Double>
) : ViewModel() {

    val originCoinsData = mutableListOf<CoinDataItem>()

    private val _initLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initLoadingData: LiveData<LoadingData<Unit>> = _initLoadingData

    private val _sellLoadingData = MutableLiveData<LoadingData<Unit>>()
    val sellLoadingData: LiveData<LoadingData<Unit>> = _sellLoadingData

    private val _usdAmount = MutableLiveData(0.0)
    val usdAmount: LiveData<Double> = _usdAmount

    private val _usdAmountError = MutableLiveData<String>()
    val usdAmountError: LiveData<String> = _usdAmountError

    private val _selectedCoinModel = MutableLiveData<CoinPresentationModel>()
    val selectedCoinModel: LiveData<CoinPresentationModel> = _selectedCoinModel

    private val _selectedCoin = MutableLiveData<CoinDataItem>()
    val selectedCoin: LiveData<CoinDataItem> = _selectedCoin

    private val _rate = MutableLiveData<AtmSellRateModelView>()
    val rate: LiveData<AtmSellRateModelView> = _rate

    private val coinAmount: LiveData<Double> =
        DoubleCombinedLiveData(usdAmount, selectedCoin) { amount, coin ->
            val feePercent = serviceInfoProvider.getServiceFee(ServiceType.ATM_SELL)
            val price = coin?.priceUsd ?: 0.0
            (amount ?: 0.0) / price * (1 + feePercent / 100)
        }

    val formattedCoinAmount: LiveData<String> =
        DoubleCombinedLiveData(coinAmount, selectedCoin) { amount, coin ->
            "${amount?.toStringCoin().orEmpty()} ${coin?.code.orEmpty()}"
        }

    val fee: LiveData<AtmSellFeeModelView> =
        DoubleCombinedLiveData(usdAmount, selectedCoin) { amount, coin ->
            val feePercent = serviceInfoProvider.getServiceFee(ServiceType.ATM_SELL)
            val price = coin?.priceUsd ?: 0.0
            AtmSellFeeModelView(
                feePercent,
                (amount ?: 0.0) / price * (feePercent / 100),
                coin?.code.orEmpty()
            )
        }

    private val _todayLimit = MutableLiveData<Double>()

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
        val maxAmount = coinLimitsValueProvider.getMaxValue(currentCoinToSend, 0.0)
        setAmount(maxAmount * currentCoinToSend.priceUsd)
    }

    fun setCoin(coin: CoinDataItem) {
        if (coin != selectedCoin.value) {
            updateCoin(coin)
        }
    }

    fun setAmount(sendAmount: Double) {
        _usdAmount.value = sendAmount
    }

    fun sell() {
        val coin = _selectedCoin.value ?: return
        val amount = coinAmount.value ?: return
        val usdAmount = usdAmount.value ?: return
        val todayLimit = _todayLimit.value ?: return
        val fee = serviceInfoProvider.getServiceFee(ServiceType.ATM_SELL)
        if (amount > coin.reservedBalanceCoin) {
            _usdAmountError.value = stringProvider.getString(R.string.sell_amount_exceeds_limit)
            return
        }
        if (usdAmount > todayLimit) {
            _usdAmountError.value =
                stringProvider.getString(R.string.sell_amount_exceeds_today_limit)
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
        _selectedCoinModel.value = CoinPresentationModel(
            coin.code, coin.balanceCoin, 0.0
        )
    }

    private fun loadLimits(coinDataItem: CoinDataItem) {
        sellGetLimitsUseCase(Unit,
            onSuccess = {
                _todayLimit.value = it.todayLimit
                _txLimitFormatted.value = priceFormatter.format(it.txLimit)
                _dailyLimitFormatted.value = priceFormatter.format(it.dailyLimit)
                _todayLimitFormatted.value = priceFormatter.format(it.todayLimit)
                updateCoin(coinDataItem)
                _usdAmount.value = 0.0
                _initLoadingData.value = LoadingData.Success(Unit)
            },
            onError = { _initLoadingData.value = LoadingData.Error(it) }
        )
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