package com.belcobtm.presentation.screens.wallet.trade.order.details

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.model.order.OrderStatus
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.domain.trade.order.CancelOrderUseCase
import com.belcobtm.domain.trade.order.ObserveMissedMessageCountUseCase
import com.belcobtm.domain.trade.order.ObserveOrderDetailsUseCase
import com.belcobtm.domain.trade.order.UpdateOrderStatusUseCase
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.screens.wallet.trade.list.model.OrderItem
import com.belcobtm.presentation.screens.wallet.trade.list.model.OrderStatusItem
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradePayment
import com.belcobtm.presentation.screens.wallet.trade.order.details.model.OrderActionButtonsState
import com.belcobtm.presentation.screens.wallet.trade.order.details.model.UpdateOrderStatusItem
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.formatter.Formatter
import com.belcobtm.presentation.tools.formatter.GoogleMapsDirectionQueryFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TradeOrderDetailsViewModel(
    private val observeMissedMessageCountUseCase: ObserveMissedMessageCountUseCase,
    private val observeOrderDetailsUseCase: ObserveOrderDetailsUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val stringProvider: StringProvider,
    private val googleMapQueryFormatter: Formatter<GoogleMapsDirectionQueryFormatter.Location>,
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

    private val _tradeType = MutableLiveData<TradeType>()
    val tradeType: LiveData<TradeType> = _tradeType

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private val _partnerTradeRate = MutableLiveData<Double>()
    val partnerTradeRate: LiveData<Double> = _partnerTradeRate

    private val _partnerTotalTrades = MutableLiveData<String>()
    val partnerTotalTrades: LiveData<String> = _partnerTotalTrades

    private val _traderStatusIcon = MutableLiveData<@DrawableRes Int>()
    val traderStatusIcon: LiveData<Int> = _traderStatusIcon

    private val _distance = MutableLiveData<String>()
    val distance: LiveData<String> = _distance

    private val _terms = MutableLiveData<String>()
    val terms: LiveData<String> = _terms

    private val _paymentOptions = MutableLiveData<List<TradePayment>>()
    val paymentOptions: LiveData<List<TradePayment>> = _paymentOptions

    private val _orderStatus = MutableLiveData<OrderStatusItem>()
    val orderStatus: LiveData<OrderStatusItem> = _orderStatus

    private val _orderId = MutableLiveData<String>()
    val orderId: LiveData<String> = _orderId

    private val _myScore = MutableLiveData<Int>()
    val myScore: LiveData<Int> = _myScore

    private val _partnerScore = MutableLiveData<Int>()
    val partnerScore: LiveData<Int> = _partnerScore

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

    private var partnerLat: Double? = null
    private var partnerLong: Double? = null
    private lateinit var order: OrderItem
    private var isBuyer: Boolean = false

    fun fetchInitialData(orderId: String) {
        viewModelScope.launch {
            _initialLoadingData.value = LoadingData.Loading()
            observeOrderDetailsUseCase(orderId)
                .collectLatest {
                    if (it.isRight) {
                        (it as Either.Right<OrderItem?>).b?.let { order ->
                            showContent(
                                order = order
                            )
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
            .asLiveData(viewModelScope.coroutineContext)

    fun updateOrderPrimaryAction(orderId: String) {
        val newStatus = buttonsState.value?.primaryStatus ?: return
        if (newStatus != OrderStatus.UNKNOWN) {
            updateStatus(orderId, newStatus, _primaryActionUpdateLoadingData)
        }
    }

    fun updateOrderSecondaryAction(orderId: String) {
        val newStatus = buttonsState.value?.secondaryStatus ?: return
        if (newStatus != OrderStatus.UNKNOWN) {
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
        status: OrderStatus,
        loadingData: MutableLiveData<LoadingData<Unit>>
    ) {
        loadingData.value = LoadingData.Loading()
        if (status == OrderStatus.CANCELED) {
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
        val isOrderResolved = with(order.orderStatus) {
            statusId == OrderStatus.RELEASED || statusId == OrderStatus.SOLVED
        }
        if (order.myUserId == order.makerId) {
            _myScore.value = order.makerRate
            _partnerScore.value = order.takerRate
            this._partnerPublicId.value = order.takerPublicId
            partnerLat = order.takerLatitude
            partnerLong = order.takerLongitude
            _partnerTradeRate.value = order.takerTradingRate ?: 0.0
            _partnerTotalTrades.value = order.takerTotalTradesFormatted
            _openRateScreen.value = order.makerRate == 0 && isOrderResolved
        } else {
            _myScore.value = order.takerRate
            _partnerScore.value = order.makerRate
            this._partnerPublicId.value = order.makerPublicId
            partnerLat = order.makerLatitude
            partnerLong = order.makerLongitude
            _partnerTradeRate.value = order.makerTradingRate ?: 0.0
            _partnerTotalTrades.value = order.makerTotalTradesFormatted
            _openRateScreen.value = order.takerRate == 0 && isOrderResolved
        }
        this.order = order
        isBuyer = order.mappedTradeType == TradeType.BUY
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
                    primaryStatus = OrderStatus.DOING,
                    secondaryStatus = OrderStatus.CANCELED
                )
            OrderStatus.DOING ->
                OrderActionButtonsState(
                    primaryButtonTitleRes = R.string.trade_order_details_screen_update_paid_button_title,
                    secondaryButtonTitleRes = R.string.trade_order_details_screen_update_cancel_button_title,
                    showPrimaryButton = true,
                    showSecondaryButton = false,
                    primaryStatus = OrderStatus.PAID,
                    secondaryStatus = OrderStatus.CANCELED
                )
            OrderStatus.PAID ->
                OrderActionButtonsState(
                    secondaryButtonTitleRes = R.string.trade_order_details_screen_update_dispute_button_title,
                    showPrimaryButton = false,
                    showSecondaryButton = true,
                    secondaryStatus = OrderStatus.DISPUTING
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
                    secondaryStatus = OrderStatus.CANCELED
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
                    primaryStatus = OrderStatus.RELEASED,
                    secondaryStatus = OrderStatus.DISPUTING
                )
            else -> OrderActionButtonsState()
        }

}
