package com.belcobtm.presentation.features.wallet.trade.mytrade.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.data.model.trade.TradeStatus
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.trade.details.CancelTradeUseCase
import com.belcobtm.domain.trade.details.ObserveTradeDetailsUseCase
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.UpdateReservedBalanceUseCase
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.features.wallet.trade.list.model.TradeItem
import com.belcobtm.presentation.features.wallet.trade.list.model.TradePayment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MyTradeDetailsViewModel(
    private val observeTradeDetailsUseCase: ObserveTradeDetailsUseCase,
    private val cancelTradeUseCase: CancelTradeUseCase,
    private val stringProvider: StringProvider,
    private val serviceInfoProvider: ServiceInfoProvider,
    private val updateReservedBalanceUseCase: UpdateReservedBalanceUseCase,
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _cancelTradeLoadingData = MutableLiveData<LoadingData<Unit>>()
    val cancelTradeLoadingData: LiveData<LoadingData<Unit>> = _cancelTradeLoadingData

    private val _selectedCoin = MutableLiveData<LocalCoinType>()
    val selectedCoin: LiveData<LocalCoinType> = _selectedCoin

    private val _tradeType = MutableLiveData<@TradeType Int>()
    val tradeType: LiveData<@TradeType Int> = _tradeType

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private val _ordersCount = MutableLiveData<Int>()
    val ordersCount: LiveData<Int> = _ordersCount

    private val _terms = MutableLiveData<String>()
    val terms: LiveData<String> = _terms

    private val _paymentOptions = MutableLiveData<List<TradePayment>>()
    val paymentOptions: LiveData<List<TradePayment>> = _paymentOptions

    private val _amountRange = MutableLiveData<String>()
    val amountRange: LiveData<String> = _amountRange

    private val _isOutOfStock = MutableLiveData<Boolean>()
    val isOutOfStock: LiveData<Boolean> = _isOutOfStock

    private val _isCancelled = MutableLiveData<Boolean>()
    val isCancelled: LiveData<Boolean> = _isCancelled

    private lateinit var trade: TradeItem

    fun fetchTradeDetails(tradeId: String) {
        _initialLoadingData.value = LoadingData.Loading()
        viewModelScope.launch {
            observeTradeDetailsUseCase(tradeId)
                .filterNotNull()
                .collectLatest {
                    if (it.isRight) {
                        updateTradeData((it as Either.Right<TradeItem>).b)
                    } else {
                        _initialLoadingData.value = LoadingData.Error(
                            (it as Either.Left<Failure>).a
                        )
                    }
                }
        }
    }

    private fun updateTradeData(trade: TradeItem) {
        this.trade = trade
        _selectedCoin.value = trade.coin
        _price.value = trade.priceFormatted
        _tradeType.value = trade.tradeType
        val isOutOfStock = trade.minLimit > trade.maxLimit
        _isOutOfStock.value = isOutOfStock
        _amountRange.value = if (isOutOfStock) {
            stringProvider.getString(R.string.trade_amount_range_out_of_stock)
        } else {
            stringProvider.getString(
                R.string.trade_list_item_price_range_format,
                trade.minLimitFormatted,
                trade.maxLimitFormatted
            )
        }
        _paymentOptions.value = trade.paymentMethods
        _ordersCount.value = trade.ordersCount
        _terms.value = trade.terms
        _isCancelled.value = trade.status == TradeStatus.CANCELLED
        _initialLoadingData.value = LoadingData.Success(Unit)
    }

    fun cancel(tradeId: String) {
        _cancelTradeLoadingData.value = LoadingData.Loading()
        val feePercent = serviceInfoProvider.getServiceFee(ServiceType.TRADE)
        cancelTradeUseCase(tradeId, onSuccess = {
            updateReservedBalanceUseCase(
                params = UpdateReservedBalanceUseCase.Params(
                    coinCode = trade.coin.name,
                    txAmount = -1 * trade.maxLimit * (1 + feePercent / 100 / 2),
                    txCryptoAmount = -1 * trade.maxLimit / trade.price * (1 + feePercent / 100 / 2),
                    txFee = 0.0,
                    maxAmountUsed = false
                ),
                onSuccess = {
                    _cancelTradeLoadingData.value = LoadingData.Success(Unit)
                },
                onError = {
                    _cancelTradeLoadingData.value = LoadingData.Error(it)
                }
            )
        }, onError = {
            _cancelTradeLoadingData.value = LoadingData.Error(it)
        })
    }
}
