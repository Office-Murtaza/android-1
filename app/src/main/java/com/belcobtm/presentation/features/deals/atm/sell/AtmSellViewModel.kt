package com.belcobtm.presentation.features.deals.atm.sell

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.interactor.SellGetLimitsUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.coin.CoinLimitsValueProvider
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.deals.swap.CoinPresentationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AtmSellViewModel(
    private val getCoinListUseCase: GetCoinListUseCase,
    private val sellGetLimitsUseCase: SellGetLimitsUseCase,
    private val accountDao: AccountDao,
    private val coinLimitsValueProvider: CoinLimitsValueProvider,
    private val priceFormatter: Formatter<Double>,
) : ViewModel() {

    val originCoinsData = mutableListOf<CoinDataItem>()

    private val _initLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initLoadingData: LiveData<LoadingData<Unit>> = _initLoadingData

    private val _coinAmount = MutableLiveData(0.0)
    val coinAmount: LiveData<Double> = _coinAmount

    private val _usdAmount = MutableLiveData(0.0)
    val usdAmount: LiveData<Double> = _usdAmount

    private val _selectedCoinModel = MutableLiveData<CoinPresentationModel>()
    val selectedCoinModel: LiveData<CoinPresentationModel> = _selectedCoinModel

    private val _selectedCoin = MutableLiveData<CoinDataItem>()
    val selectedCoin: LiveData<CoinDataItem> = _selectedCoin

    private val _rate = MutableLiveData<AtmSellRateModelView>()
    val rate: LiveData<AtmSellRateModelView> = _rate

    private val _fee = MutableLiveData<AtmSellFeeModelView>()
    val fee: LiveData<AtmSellFeeModelView> = _fee

    private val _todayLimit = MutableLiveData<String>()
    val todayLimit: LiveData<String> = _todayLimit

    private val _txLimit = MutableLiveData<String>()
    val txLimit: LiveData<String> = _txLimit

    private val _dailyLimit = MutableLiveData<String>()
    val dailyLimit: LiveData<String> = _dailyLimit


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
                    _initLoadingData.value = LoadingData.Success(Unit)
                    if (originCoinsData.size >= 2) {
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
        val maxAmount = coinLimitsValueProvider
            .getMaxValue(currentCoinToSend)
        setSendAmount(maxAmount)
    }

    fun setCoinToSend(coin: CoinDataItem) {
        if (coin != selectedCoin.value) {
            updateCoin(coin)
        }
    }

    fun setSendAmount(sendAmount: Double) {
        _coinAmount.value = sendAmount
        val sendCoin = _selectedCoin.value ?: return
        _usdAmount.value = sendAmount / sendCoin.priceUsd  * (1 + )
    }

    private fun updateCoin(coin: CoinDataItem) {
        // clear up the values to operate with 0 amount in the callbacks
        _selectedCoin.value = coin
        val sendAmount = _coinAmount.value ?: 0.0
        _rate.value = AtmSellRateModelView(1, coin.code, coin.priceUsd)
        _selectedCoinModel.value = CoinPresentationModel(
            coin.code, coin.balanceCoin, coin.details.txFee
        )
        // notify UI that coin details has beed successfully fetched
//        _coinsDetailsLoadingState.value = LoadingData.Success(Unit)
    }

    private fun loadLimits(coinDataItem: CoinDataItem) {
        sellGetLimitsUseCase(Unit,
            onSuccess = {
                _txLimit.value = priceFormatter.format(it.txLimit)
                _dailyLimit.value = priceFormatter.format(it.dailyLimit)
                _todayLimit.value = priceFormatter.format(it.todayLimit)
                updateCoin(coinDataItem)
                        },
            onError = { _initLoadingData.value = LoadingData.Error(it) }
        )
    }
}

data class AtmSellFeeModelView(
    val platformFeePercents: Double,
    val platformFeeCoinAmount: Double,
    val swapCoinCode: String
)

data class AtmSellRateModelView(
    val coinAmount: Int,
    val coinCode: String,
    val usdAmount: Double
)