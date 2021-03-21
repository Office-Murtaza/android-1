package com.app.belcobtm.presentation.features.wallet.trade.order.details

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.OrderStatus
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.order.ObserveOrderDetailsUseCase
import com.app.belcobtm.domain.trade.order.UpdateOrderStatusUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderItem
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderStatusItem
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradePayment
import com.app.belcobtm.presentation.features.wallet.trade.order.details.model.OrderActionButtonsState
import com.app.belcobtm.presentation.features.wallet.trade.order.details.model.UpdateOrderStatusItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TradeOrderDetailsViewModel(
    private val observeOrderDetailsUseCase: ObserveOrderDetailsUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _primaryActionUpdateLoadingData = MutableLiveData<LoadingData<Unit>>()
    val primaryActionUpdateLoadingData: LiveData<LoadingData<Unit>> = _primaryActionUpdateLoadingData

    private val _secondaryActionUpdateLoadingData = MutableLiveData<LoadingData<Unit>>()
    val secondaryActionUpdateLoadingData: LiveData<LoadingData<Unit>> = _secondaryActionUpdateLoadingData

    private val _coin = MutableLiveData<LocalCoinType>()
    val coin: LiveData<LocalCoinType> = _coin

    private val _tradeType = MutableLiveData<@TradeType Int>()
    val tradeType: LiveData<@TradeType Int> = _tradeType

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private val _makerPublicId = MutableLiveData<String>()
    val makerPublicId: LiveData<String> = _makerPublicId

    private val _traderStatus = MutableLiveData<@DrawableRes Int>()
    val traderStatus: LiveData<Int> = _traderStatus

    private val _distance = MutableLiveData<String>()
    val distance: LiveData<String> = _distance

    private val _terms = MutableLiveData<String>()
    val terms: LiveData<String> = _terms

    private val _paymentOptions = MutableLiveData<List<TradePayment>>()
    val paymentOptions: LiveData<List<TradePayment>> = _paymentOptions

    private val _orderStatus = MutableLiveData<OrderStatusItem>()
    val orderStatus: LiveData<OrderStatusItem> = _orderStatus

    private val _myScore = MutableLiveData<Double>()
    val myScore: LiveData<Double> = _myScore

    private val _partnerScore = MutableLiveData<Double>()
    val partnerScore: LiveData<Double> = _partnerScore

    private val _partnerPublicId = MutableLiveData<String>()
    val partnerPublicId: LiveData<String> = _partnerPublicId

    private val _partnerTotalTrades = MutableLiveData<Int>()
    val partnerTotalTrades: LiveData<Int> = _partnerTotalTrades

    private val _cryptoAmount = MutableLiveData<String>()
    val cryptoAmount: LiveData<String> = _cryptoAmount

    private val _fiatAmount = MutableLiveData<String>()
    val fiatAmount: LiveData<String> = _fiatAmount

    private val _buttonsState = MutableLiveData<OrderActionButtonsState>()
    val buttonsState: LiveData<OrderActionButtonsState> = _buttonsState

    fun fetchInitialData(orderId: Int) {
        viewModelScope.launch {
            _initialLoadingData.value = LoadingData.Loading()
            observeOrderDetailsUseCase(orderId)
                .collect {
                    if (it.isRight) {
                        showContent((it as Either.Right<OrderItem>).b)
                    } else {
                        _initialLoadingData.value = LoadingData.Error((it as Either.Left<Failure>).a)
                    }
                }
        }
    }

    fun updateOrderPrimaryAction(orderId: Int) {
        val newStatus = buttonsState.value?.primaryStatusId ?: return
        if (newStatus != OrderStatus.UNDEFINED) {
            updateStatus(orderId, newStatus, _primaryActionUpdateLoadingData)
        }
    }

    fun updateOrderSecondaryAction(orderId: Int) {
        val newStatus = buttonsState.value?.primaryStatusId ?: return
        if (newStatus != OrderStatus.UNDEFINED) {
            updateStatus(orderId, newStatus, _secondaryActionUpdateLoadingData)
        }
    }

    private fun updateStatus(orderId: Int, @OrderStatus status: Int, loadingData: MutableLiveData<LoadingData<Unit>>) {
        loadingData.value = LoadingData.Loading()
        updateOrderStatusUseCase(
            UpdateOrderStatusItem(orderId, status),
            onSuccess = { loadingData.value = LoadingData.Success(Unit) },
            onError = { loadingData.value = LoadingData.Error(it) }
        )
    }

    private fun showContent(order: OrderItem) {
        _coin.value = order.coin
        _terms.value = order.terms
        _price.value = order.trade.priceFormatted
        _orderStatus.value = order.orderStatus
        _paymentOptions.value = order.trade.paymentMethods
        _tradeType.value = order.mappedTradeType
        _cryptoAmount.value = stringProvider.getString(
            R.string.trade_order_details_crypto_amount_formatted,
            order.cryptoAmount.toStringCoin(),
            order.coin.name
        )
        _fiatAmount.value = order.fiatAmountFormatted
        if (order.myTradeId == order.makerId) {
            _myScore.value = order.makerTradingRate
            _partnerScore.value = order.takerTradingRate
            _partnerPublicId.value = order.takerPublicId
            _partnerTotalTrades.value = order.takerTotalTrades
        } else {
            _myScore.value = order.takerTradingRate
            _partnerScore.value = order.makerTradingRate
            _partnerPublicId.value = order.makerPublicId
            _partnerTotalTrades.value = order.makerTotalTrades
        }
        val isBuyer = order.mappedTradeType == TradeType.BUY
        _buttonsState.value = if (isBuyer) setupBuyerButtons(order) else setupSellerButtons(order)
        _initialLoadingData.value = LoadingData.Success(Unit)
    }

    private fun setupBuyerButtons(order: OrderItem): OrderActionButtonsState =
        when (order.orderStatus.statusId) {
            OrderStatus.NEW ->
                OrderActionButtonsState(
                    primaryButtonTitleRes = R.string.trade_order_details_screen_update_doing_button_title,
                    secondaryButtonTitleRes = R.string.trade_order_details_screen_update_cancel_button_title,
                    showPrimaryButton = true,
                    showSecondaryButton = true,
                    primaryStatusId = OrderStatus.DOING,
                    secondaryStatusId = OrderStatus.CANCELLED
                )
            OrderStatus.DOING ->
                OrderActionButtonsState(
                    primaryButtonTitleRes = R.string.trade_order_details_screen_update_paid_button_title,
                    secondaryButtonTitleRes = R.string.trade_order_details_screen_update_cancel_button_title,
                    showPrimaryButton = true,
                    showSecondaryButton = true,
                    primaryStatusId = OrderStatus.PAID,
                    secondaryStatusId = OrderStatus.CANCELLED
                )
            OrderStatus.PAID ->
                OrderActionButtonsState(
                    primaryButtonTitleRes = R.string.trade_order_details_screen_update_dispute_button_title,
                    showPrimaryButton = true,
                    showSecondaryButton = false,
                    primaryStatusId = OrderStatus.DISPUTING
                )
            else -> OrderActionButtonsState()
        }

    private fun setupSellerButtons(order: OrderItem): OrderActionButtonsState =
        when (order.orderStatus.statusId) {
            OrderStatus.NEW ->
                OrderActionButtonsState(
                    secondaryButtonTitleRes = R.string.trade_order_details_screen_update_cancel_button_title,
                    showPrimaryButton = false,
                    showSecondaryButton = true,
                    primaryStatusId = OrderStatus.CANCELLED
                )
            OrderStatus.DOING, OrderStatus.PAID ->
                OrderActionButtonsState(
                    primaryButtonTitleRes = R.string.trade_order_details_screen_update_release_button_title,
                    secondaryButtonTitleRes = R.string.trade_order_details_screen_update_dispute_button_title,
                    showPrimaryButton = true,
                    showSecondaryButton = true,
                    primaryStatusId = OrderStatus.RELEASED,
                    secondaryStatusId = OrderStatus.DISPUTING
                )
            else -> OrderActionButtonsState()
        }
}