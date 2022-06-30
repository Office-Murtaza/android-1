package com.belcobtm.data.inmemory.trade

import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.data.helper.DistanceCalculator
import com.belcobtm.data.mapper.OrderResponseToOrderMapper
import com.belcobtm.data.mapper.TradeResponseToTradeMapper
import com.belcobtm.data.mapper.TradesResponseToTradeDataMapper
import com.belcobtm.data.model.trade.Order
import com.belcobtm.data.model.trade.Trade
import com.belcobtm.data.model.trade.TradeData
import com.belcobtm.data.model.trade.filter.TradeFilter
import com.belcobtm.data.rest.trade.response.TradeItemResponse
import com.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.belcobtm.data.rest.trade.response.TradesResponse
import com.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.flatMap
import com.belcobtm.domain.map
import com.belcobtm.domain.trade.order.mapper.ChatMessageMapper
import com.belcobtm.presentation.screens.wallet.trade.list.filter.model.TradeFilterItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TradeInMemoryCache(
    private val tradesMapper: TradesResponseToTradeDataMapper,
    private val distanceCalculator: DistanceCalculator,
    private val distanceCalculatorScope: CoroutineScope,
    private val orderMapper: OrderResponseToOrderMapper,
    private val tradeMapper: TradeResponseToTradeMapper,
    private val chatMessageMapper: ChatMessageMapper,
    private val cacheDispatcher: CoroutineDispatcher,
    private val filterDispatcher: CoroutineDispatcher,
    private val chatDispatcher: CoroutineDispatcher,
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

    lateinit var filterItem: TradeFilterItem

    var calculateDistance = false
        private set

    lateinit var enabledCoins: List<AccountEntity>
        private set

    private var distanceCalculationJob: Job? = null

    suspend fun updateCache(
        needCalculateDistance: Boolean,
        response: Either<Failure, TradesResponse>
    ) {
        withContext(cacheDispatcher) {
            calculateDistance = needCalculateDistance
            if (response.isLeft) {
                cache.value = response as Either.Left<Failure>
            } else {
                cache.value =
                    Either.Right(tradesMapper.map((response as Either.Right<TradesResponse>).b))
                startDistanceCalculation()
            }
        }
    }

    suspend fun updateFilter(filter: TradeFilter, filterItem: TradeFilterItem) {
        withContext(filterDispatcher) {
            tradeFilter.value = filter
            this@TradeInMemoryCache.filterItem = filterItem
        }
    }

    fun findTrade(tradeId: String): Either<Failure, Trade> {
        val currentCache = cache.value ?: return Either.Left(Failure.ServerError())
        return currentCache.flatMap { tradeData ->
            tradeData.trades[tradeId]?.let { Either.Right(it) }
                ?: Either.Left(Failure.ServerError())
        }
    }

    fun findOrder(orderId: String): Either<Failure, Order> {
        val currentCache = cache.value ?: return Either.Left(Failure.ServerError())
        return currentCache.flatMap { tradeData ->
            tradeData.orders[orderId]?.let { Either.Right(it) }
                ?: Either.Left(Failure.ServerError())
        }
    }

    suspend fun updateTrades(trade: TradeItemResponse) {
        withContext(cacheDispatcher) {
            cache.value?.map {
                val mappedTrade = tradeMapper.map(trade)
                val trades = HashMap(it.trades)
                trades[mappedTrade.id] = mappedTrade
                cache.value = Either.Right(it.copy(trades = trades))
                startDistanceCalculation()
            }
        }
    }

    suspend fun updateOrders(order: TradeOrderItemResponse) {
        withContext(cacheDispatcher) {
            cache.value?.map {
                val mappedOrder = orderMapper.map(order, it.orders[order.id]?.chatHistory.orEmpty())
                val orders = HashMap(it.orders)
                orders[mappedOrder.id] = mappedOrder
                cache.value = Either.Right(it.copy(orders = orders))
            }
        }
    }

    suspend fun updateLastSeenMessageTimestamp() {
        withContext(chatDispatcher) {
            lastSeenMessageTimestamp.value = System.currentTimeMillis()
        }
    }

    suspend fun clearCache() {
        withContext(cacheDispatcher) {
            cache.value = null
        }
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
                withContext(cacheDispatcher) {
                    cache.value = Either.Right(tradeData.copy(trades = tradesWithDistance))
                }
            }
        }
    }

    suspend fun updateChat(response: ChatMessageResponse) {
        withContext(chatDispatcher) {
            val mappedMessage = chatMessageMapper.map(response, isFromHistory = false)
            cache.value?.map {
                val chatOrder = it.orders.getValue(response.orderId)
                val orders = HashMap(it.orders)
                orders[chatOrder.id] =
                    chatOrder.copy(chatHistory = chatOrder.chatHistory + mappedMessage)
                cache.value = Either.Right(it.copy(orders = orders))
            }
        }
    }

    fun initCoins(enabledCoins: List<AccountEntity>) {
        this.enabledCoins = enabledCoins
    }

}
