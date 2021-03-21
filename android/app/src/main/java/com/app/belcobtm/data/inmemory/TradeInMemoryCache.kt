package com.app.belcobtm.data.inmemory

import com.app.belcobtm.data.helper.DistanceCalculator
import com.app.belcobtm.data.mapper.OrderResponseToOrderMapper
import com.app.belcobtm.data.mapper.TradesResponseToTradeDataMapper
import com.app.belcobtm.data.model.trade.Order
import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.filter.TradeFilter
import com.app.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.app.belcobtm.data.rest.trade.response.TradesResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TradeInMemoryCache(
    private val tradesMapper: TradesResponseToTradeDataMapper,
    private val distanceCalculator: DistanceCalculator,
    private val distanceCalculatorScope: CoroutineScope,
    private val orderMapper: OrderResponseToOrderMapper
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

    val observableData: StateFlow<Either<Failure, TradeData>?>
        get() = cache

    val data: Either<Failure, TradeData>?
        get() = cache.value

    val observableFilter: StateFlow<TradeFilter?>
        get() = tradeFilter

    val filter: TradeFilter?
        get() = tradeFilter.value

    var calculateDistance = false
        private set

    private var distanceCalculationJob: Job? = null

    fun updateCache(calculateDistance: Boolean, response: Either<Failure, TradesResponse>) {
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

    fun updateDistances(distances: Map<Int, Double>) {
        val currentCache = cache.value
        val tradeData: TradeData =
            (currentCache?.takeIf { currentCache.isRight } as? Either.Right<TradeData>)?.b ?: return
        cache.value = Either.Right(tradeData.copy(
            trades = tradeData.trades.asSequence()
                .map { it.copy(distance = distances[it.id] ?: UNDEFINED_DISTANCE) }
                .sortedBy { it.distance }
                .toList()
        ))
    }

    fun updateOrders(order: TradeOrderItemResponse) {
        val currentCache = cache.value
        val mappedOrder = orderMapper.map(order)
        val tradeData: TradeData =
            (currentCache?.takeIf { currentCache.isRight } as? Either.Right<TradeData>)?.b ?: return
        val isNewOrder = tradeData.orders.none { it.id == order.id }
        val updatedCache = if (isNewOrder) {
            tradeData.copy(orders = tradeData.orders + mappedOrder)
        } else {
            tradeData.copy(
                orders = tradeData.orders.asSequence()
                    .map { if (it.id == order.id) mappedOrder else it }
                    .toList()
            )
        }
        cache.value = Either.Right(updatedCache)
    }

    fun findTrade(tradeId: Int): Either<Failure, Trade> {
        val currentCache = cache.value ?: return Either.Left(Failure.ServerError())
        return if (currentCache.isLeft) {
            currentCache as Either.Left<Failure>
        } else {
            val tradeData = (currentCache as Either.Right<TradeData>).b
            tradeData.trades.find { it.id == tradeId }
                ?.let { Either.Right(it) }
                ?: Either.Left(Failure.ServerError())
        }
    }

    fun cleanCache() {
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

    fun findOrder(orderId: Int): Either<Failure, Order> {
        val currentCache = cache.value ?: return Either.Left(Failure.ServerError())
        return if (currentCache.isLeft) {
            currentCache as Either.Left<Failure>
        } else {
            val tradeData = (currentCache as Either.Right<TradeData>).b
            tradeData.orders.find { it.id == orderId }
                ?.let { Either.Right(it) }
                ?: Either.Left(Failure.ServerError())
        }
    }
}