package com.belcobtm.data.inmemory.trade

import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.data.helper.DistanceCalculator
import com.belcobtm.data.rest.trade.response.TradeHistoryResponse
import com.belcobtm.data.rest.trade.response.TradeOrderResponse
import com.belcobtm.data.rest.trade.response.TradeResponse
import com.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.flatMap
import com.belcobtm.domain.map
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.filter.TradeFilter
import com.belcobtm.domain.trade.model.order.OrderDomainModel
import com.belcobtm.domain.trade.model.trade.TradeDomainModel
import com.belcobtm.domain.trade.order.mapper.ChatMessageMapper
import com.belcobtm.domain.wallet.LocalCoinType
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
    private val distanceCalculator: DistanceCalculator,
    private val distanceCalculatorScope: CoroutineScope,
    private val chatMessageMapper: ChatMessageMapper,
    private val cacheDispatcher: CoroutineDispatcher,
    private val filterDispatcher: CoroutineDispatcher,
    private val chatDispatcher: CoroutineDispatcher,
) {

    private val cache = MutableStateFlow<Either<Failure, TradeHistoryDomainModel>?>(null)
    private val tradeFilter = MutableStateFlow<TradeFilter?>(null)
    private val lastSeenMessageTimestamp = MutableStateFlow<Long>(0)

    val observableData: StateFlow<Either<Failure, TradeHistoryDomainModel>?> = cache

    val observableLastSeenMessageTimestamp: StateFlow<Long>
        get() = lastSeenMessageTimestamp

    val data: Either<Failure, TradeHistoryDomainModel>?
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
        response: Either<Failure, TradeHistoryResponse>
    ) {
        withContext(cacheDispatcher) {
            calculateDistance = needCalculateDistance
            if (response.isLeft) {
                cache.value = response as Either.Left<Failure>
            } else {
                cache.value =
                    Either.Right(
                        (response as Either.Right<TradeHistoryResponse>).b
                            .mapToDomain(chatMessageMapper)
                    )
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

    fun findTrade(tradeId: String): Either<Failure, TradeDomainModel> {
        val currentCache = cache.value ?: return Either.Left(Failure.ServerError())
        return currentCache.flatMap { tradeData ->
            tradeData.trades[tradeId]?.let { Either.Right(it) }
                ?: Either.Left(Failure.ServerError())
        }
    }

    fun findOrder(orderId: String): Either<Failure, OrderDomainModel> {
        val currentCache = cache.value ?: return Either.Left(Failure.ServerError())
        return currentCache.flatMap { tradeData ->
            tradeData.orders[orderId]?.let { Either.Right(it) }
                ?: Either.Left(Failure.ServerError())
        }
    }

    suspend fun updateTrades(trade: TradeResponse) {
        withContext(cacheDispatcher) {
            cache.value?.map {
                val mappedTrade = trade.mapToDomain()
                val trades = HashMap(it.trades)
                trades[mappedTrade.id] = mappedTrade
                cache.value = Either.Right(it.copy(trades = trades))
                startDistanceCalculation()
            }
        }
    }

    suspend fun updateOrders(order: TradeOrderResponse) {
        withContext(cacheDispatcher) {
            cache.value?.map { history ->
                val mappedOrder = order.mapToDomain(
                    order.tradeId?.let { history.trades[it]?.coin } ?: LocalCoinType.CATM, // nothing else to make default
                    history.orders[order.id]?.chatHistory.orEmpty()
                )
                val orders = HashMap(history.orders)
                orders[mappedOrder.id] = mappedOrder
                cache.value = Either.Right(history.copy(orders = orders))
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
            if (currentCache is Either.Right<TradeHistoryDomainModel>) {
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
                it.orders.getOrDefault(
                    key = response.orderId.orEmpty(),
                    defaultValue = null
                )?.let { chatOrder ->
                    val orders = HashMap(it.orders)
                    orders[chatOrder.id] =
                        chatOrder.copy(chatHistory = chatOrder.chatHistory + mappedMessage)
                    cache.value = Either.Right(it.copy(orders = orders))
                }
            }
        }
    }

    fun initCoins(enabledCoins: List<AccountEntity>) {
        this.enabledCoins = enabledCoins
    }

}
