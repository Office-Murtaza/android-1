package com.belcobtm.presentation.features.wallet.trade.order.details

import androidx.annotation.DrawableRes
import androidx.lifecycle.*
import com.belcobtm.R
import com.belcobtm.data.model.trade.OrderStatus
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.order.*
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.formatter.GoogleMapsDirectionQueryFormatter
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.features.wallet.trade.list.model.OrderItem
import com.belcobtm.presentation.features.wallet.trade.list.model.OrderStatusItem
import com.belcobtm.presentation.features.wallet.trade.list.model.TradePayment
import com.belcobtm.presentation.features.wallet.trade.order.details.model.OrderActionButtonsState
import com.belcobtm.presentation.features.wallet.trade.order.details.model.UpdateOrderStatusItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TradeOrderDetailsViewModel(
    private val connectToChatUseCase: ConnectToChatUseCase,
    private val disconnectFromChatUseCase: DisconnectFromChatUseCase,
    private val observeMissedMessageCountUseCase: ObserveMissedMessageCountUseCase,
    private val observeOrderDetailsUseCase: ObserveOrderDetailsUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val stringProvider: StringProvider,
    private val googleMapQueryFormatter: Formatter<GoogleMapsDirectionQueryFormatter.Location>
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _primaryActionUpdateLoadingData = MutableLiveData<LoadingData<Unit>>()
    val primaryActionUpdateLoadingData: LiveData<LoadingData<Unit>> =
        _primaryActionUpdateLoadingData

    private val _secondaryActionUpdateLoadingData = MutableLiveData<LoadingData<Unit>>()
    val secondaryActionUpdateLoadingData: LiveData<LoadingData<Unit>> =
        _secondaryActionUpdateLoadingData

    private val _coin = MutableLiveData<LocalCoinType>()
    val coin: LiveData<LocalCoinType> = _coin

    private val _tradeType = MutableLiveData<@TradeType Int>()
    val tradeType: LiveData<@TradeType Int> = _tradeType

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private val _partnerTradeRate = MutableLiveData<Double>()
    val partnerTradeRate: LiveData<Double> = _partnerTradeRate

    private val _partnerTotalTrades = MutableLiveData<String>()
    val partnerTotalTrades: LiveData<String> = _partnerTotalTrades

    private val _traderStatusIcon = MutableLiveData<@DrawableRes Int>()
    val traderStatusIcon: LiveData<Int> = _traderStatusIcon

    private val _distance = MutableLiveData<String?>()
    val distance: LiveData<String?> = _distance

    private val _terms = MutableLiveData<String>()
    val terms: LiveData<String> = _terms

    private val _paymentOptions = MutableLiveData<List<TradePayment>>()
    val paymentOptions: LiveData<List<TradePayment>> = _paymentOptions

    private val _orderStatus = MutableLiveData<OrderStatusItem>()
    val orderStatus: LiveData<OrderStatusItem> = _orderStatus

    private val _orderId = MutableLiveData<String>()
    val orderId: LiveData<String> = _orderId

    private val _myScore = MutableLiveData<Double?>()
    val myScore: LiveData<Double?> = _myScore

    private val _partnerScore = MutableLiveData<Double?>()
    val partnerScore: LiveData<Double?> = _partnerScore

    private val _partnerPublicId = MutableLiveData<String>()
    val partnerPublicId: LiveData<String> = this._partnerPublicId

    private val _cryptoAmount = MutableLiveData<String>()
    val cryptoAmount: LiveData<String> = _cryptoAmount

    private val _fiatAmount = MutableLiveData<String>()
    val fiatAmount: LiveData<String> = _fiatAmount

    private val _buttonsState = MutableLiveData<OrderActionButtonsState>()
    val buttonsState: LiveData<OrderActionButtonsState> = _buttonsState

    private val _openRateScreen = MutableLiveData<Boolean>()
    val openRateScreen: LiveData<Boolean> = _openRateScreen

    private val _myId = MutableLiveData<String>()
    val myId: LiveData<String> = _myId

    private val _partnerId = MutableLiveData<String>()
    val partnerId: LiveData<String> = _partnerId

    private var partnerLat: Double? = null
    private var partnerLong: Double? = null

    fun fetchInitialData(orderId: String) {
        viewModelScope.launch {
            _initialLoadingData.value = LoadingData.Loading()
            observeOrderDetailsUseCase(orderId)
                .collect {
                    if (it.isRight) {
                        val orderItem = (it as Either.Right<OrderItem?>).b
                        if (orderItem != null) {
                            showContent(orderItem)
                        }
                    } else {
                        _initialLoadingData.value =
                            LoadingData.Error((it as Either.Left<Failure>).a)
                    }
                }
        }
    }

    fun observeMissedMessageCount(orderId: String) =
        observeMissedMessageCountUseCase(orderId)
            .asLiveData(Dispatchers.Default)

    fun connectToChat() {
        connectToChatUseCase.invoke(Unit)
    }

    fun disconnectFromChat() {
        disconnectFromChatUseCase.invoke(Unit)
    }

    fun updateOrderPrimaryAction(orderId: String) {
        val newStatus = buttonsState.value?.primaryStatusId ?: return
        if (newStatus != OrderStatus.UNDEFINED) {
            updateStatus(orderId, newStatus, _primaryActionUpdateLoadingData)
        }
    }

    fun updateOrderSecondaryAction(orderId: String) {
        val newStatus = buttonsState.value?.secondaryStatusId ?: return
        if (newStatus != OrderStatus.UNDEFINED) {
            updateStatus(orderId, newStatus, _secondaryActionUpdateLoadingData)
        }
    }

    fun getQueryForMap(): String? {
        val toLat = partnerLat ?: return null
        val toLong = partnerLong ?: return null
        return googleMapQueryFormatter.format(
            GoogleMapsDirectionQueryFormatter.Location(
                toLat,
                toLong
            )
        )
    }

    fun isActiveOrder(): Boolean {
        val statusId = orderStatus.value?.statusId
        return statusId == OrderStatus.NEW || statusId == OrderStatus.DOING || statusId == OrderStatus.PAID
    }

    private fun updateStatus(
        orderId: String,
        @OrderStatus status: Int,
        loadingData: MutableLiveData<LoadingData<Unit>>
    ) {
        loadingData.value = LoadingData.Loading()
        if (status == OrderStatus.CANCELLED) {
            cancelOrderUseCase(orderId, onSuccess = {
                loadingData.value = LoadingData.Success(Unit)
            }, onError = {
                loadingData.value = LoadingData.Error(it)
            })
        } else {
            updateOrderStatusUseCase(
                UpdateOrderStatusItem(orderId, status),
                onSuccess = {
                    loadingData.value = LoadingData.Success(Unit)
                },
                onError = {
                    loadingData.value = LoadingData.Error(it)
                }
            )
        }
    }

    private fun showContent(order: OrderItem) {
        _coin.value = order.coin
        _orderId.value = order.id
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
        _distance.value = order.distanceFormatted
        this._partnerPublicId.value = order.trade.makerPublicId
        val isOrderResolved = with(order.orderStatus) {
            statusId == OrderStatus.RELEASED || statusId == OrderStatus.SOLVED
        }
        if (order.myTradeId == order.makerId) {
            _myScore.value = order.makerRate
            _partnerScore.value = order.takerRate
            _openRateScreen.value = order.makerRate == null && isOrderResolved
            this._partnerPublicId.value = order.takerPublicId
            _partnerId.value = order.takerId
            partnerLat = order.takerLatitude
            partnerLong = order.takerLongitude
            _partnerTradeRate.value = order.takerTradingRate ?: 0.0
            _partnerTotalTrades.value = order.takerTotalTradesFormatted
        } else {
            _myScore.value = order.takerRate
            _partnerScore.value = order.makerRate
            _openRateScreen.value = order.takerRate == null && isOrderResolved
            this._partnerPublicId.value = order.makerPublicId
            _partnerId.value = order.makerId
            partnerLat = order.makerLatitude
            partnerLong = order.makerLongitude
            _partnerTradeRate.value = order.makerTradingRate ?: 0.0
            _partnerTotalTrades.value = order.makerTotalTradesFormatted
        }
        val isBuyer = order.mappedTradeType == TradeType.BUY
        _myId.value = order.myTradeId
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
                    showSecondaryButton = false,
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
                    secondaryStatusId = OrderStatus.CANCELLED
                )
            OrderStatus.DOING ->
                OrderActionButtonsState(
                    showPrimaryButton = false,
                    showSecondaryButton = false
                )
            OrderStatus.PAID ->
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