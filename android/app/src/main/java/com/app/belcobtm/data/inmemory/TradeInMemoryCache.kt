package com.app.belcobtm.data.inmemory

import com.app.belcobtm.data.helper.DistanceCalculator
import com.app.belcobtm.data.mapper.OrderResponseToOrderMapper
import com.app.belcobtm.data.mapper.TradeResponseToTradeMapper
import com.app.belcobtm.data.mapper.TradesResponseToTradeDataMapper
import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.filter.TradeFilter
import com.app.belcobtm.data.rest.trade.response.TradeItemResponse
import com.app.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.app.belcobtm.data.rest.trade.response.TradesResponse
import com.app.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.flatMap
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.trade.order.mapper.ChatMessageMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TradeInMemoryCache(
    private val tradesMapper: TradesResponseToTradeDataMapper,
    private val distanceCalculator: DistanceCalculator,
    private val distanceCalculatorScope: CoroutineScope,
    private val orderMapper: OrderResponseToOrderMapper,
    private val tradeMapper: TradeResponseToTradeMapper,
    private val chatMessageMapper: ChatMessageMapper
) {

    companion object {
        /**
         * Max value is set because of sorting option.
         * Trades without distance provided should be set to the bottom for distance sorting
         */
        val UNDEFINED_DISTANCE = Double.MAX_VALUE
    }

    private val cache = MutableStateFlow<Either<Failure, TradeData>?>(null)
    private val tradeFilter = MutableStateFlow<TradeFilter?>(null)
    private val lastSeenMessageTimestamp = MutableStateFlow<Long>(0)

    val observableData: StateFlow<Either<Failure, TradeData>?>
        get() = cache

    val observableLastSeenMessageTimestamp: StateFlow<Long>
        get() = lastSeenMessageTimestamp

    val data: Either<Failure, TradeData>?
        get() = cache.value

    val observableFilter: Flow<TradeFilter?>
        get() = tradeFilter

    val filter: TradeFilter?
        get() = tradeFilter.value

    var calculateDistance = false
        private set

    private var distanceCalculationJob: Job? = null

    suspend fun updateCache(calculateDistance: Boolean, response: Either<Failure, TradesResponse>) {
        this.calculateDistance = calculateDistance
        if (response.isLeft) {
            cache.value = response as Either.Left<Failure>
        } else {
            cache.value = Either.Right(tradesMapper.map((response as Either.Right<TradesResponse>).b))
            startDistanceCalculation()
        }
    }

    fun updateFilter(filter: TradeFilter) {
        tradeFilter.value = filter
    }

    fun findTrade(tradeId: String): Either<Failure, Trade> {
        val currentCache = cache.value ?: return Either.Left(Failure.ServerError())
        return currentCache.flatMap { tradeData ->
            tradeData.trades[tradeId]?.let { Either.Right(it) }
                ?: Either.Left(Failure.ServerError())
        }
    }

    fun updateTrades(trade: TradeItemResponse) {
        cache.value?.map {
            val mappedTrade = tradeMapper.map(trade)
            val trades = HashMap(it.trades)
            trades[mappedTrade.id] = mappedTrade
            cache.value = Either.Right(it.copy(trades = trades))
            startDistanceCalculation()
        }
    }

    fun updateOrders(order: TradeOrderItemResponse) {
        cache.value?.map {
            val mappedOrder = orderMapper.map(order, it.orders[order.id]?.chatHistory.orEmpty())
            val orders = HashMap(it.orders)
            orders[mappedOrder.id] = mappedOrder
            cache.value = Either.Right(it.copy(orders = orders))
        }
    }

    fun updateLastSeenMessageTimestamp() {
        lastSeenMessageTimestamp.value = System.currentTimeMillis()
    }

    fun clearCache() {
        cache.value = null
    }

    private fun startDistanceCalculation() {
        distanceCalculationJob?.cancel()
        if (!calculateDistance) {
            return
        }
        distanceCalculationJob = distanceCalculatorScope.launch(Dispatchers.Default) {
            val currentCache = cache.value ?: return@launch
            if (currentCache is Either.Right<TradeData>) {
                val tradeData = currentCache.b
                val tradesWithDistance = distanceCalculator.updateDistanceToTrades(tradeData.trades)
                cache.value = Either.Right(tradeData.copy(trades = tradesWithDistance))
            }
        }
    }

    suspend fun updateChat(response: ChatMessageResponse) {
        val mappedMessage = chatMessageMapper.map(response, isFromHistory = false)
        cache.value?.map {
            val chatOrder = it.orders.getValue(response.orderId)
            val orders = HashMap(it.orders)
            orders[chatOrder.id] = chatOrder.copy(chatHistory = chatOrder.chatHistory + mappedMessage)
            cache.value = Either.Right(it.copy(orders = orders))
        }
    }
}