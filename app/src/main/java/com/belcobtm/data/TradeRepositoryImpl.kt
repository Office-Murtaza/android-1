package com.belcobtm.data

import android.content.res.Resources
import android.location.Location
import com.belcobtm.R
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.belcobtm.data.rest.trade.TradeApiService
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.mapSuspend
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.filter.mapper.TradeFilterItemMapper
import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.filter.SortOption
import com.belcobtm.domain.trade.model.filter.TradeFilter
import com.belcobtm.domain.trade.model.order.OrderDomainModel
import com.belcobtm.domain.trade.model.trade.TradeDomainModel
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.screens.wallet.trade.create.model.CreateTradeItem
import com.belcobtm.presentation.screens.wallet.trade.edit.EditTradeItem
import com.belcobtm.presentation.screens.wallet.trade.list.filter.model.TradeFilterItem
import com.belcobtm.presentation.screens.wallet.trade.order.create.model.TradeOrderItem
import com.belcobtm.presentation.screens.wallet.trade.order.details.model.UpdateOrderStatusItem
import kotlinx.coroutines.flow.Flow

class TradeRepositoryImpl(
    private val tradeApiService: TradeApiService,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val accountDao: AccountDao,
    private val resources: Resources,
    private val mapper: TradeFilterItemMapper
) : TradeRepository {

    override fun getAvailablePaymentOptions(): List<PaymentMethodType> = listOf(
        PaymentMethodType.CASH,
        PaymentMethodType.PAYPAL,
        PaymentMethodType.VENMO,
        PaymentMethodType.CASHAPP,
        PaymentMethodType.PAYONEER
    )

    override fun observeTradeData(): Flow<Either<Failure, TradeHistoryDomainModel>?> =
        tradeInMemoryCache.observableData

    override fun observeFilter(): Flow<TradeFilter?> =
        tradeInMemoryCache.observableFilter

    override fun observeLastSeenMessageTimestamp(): Flow<Long> =
        tradeInMemoryCache.observableLastSeenMessageTimestamp

    override suspend fun updateLastSeenMessageTimestamp() {
        tradeInMemoryCache.updateLastSeenMessageTimestamp()
    }

    override fun getTradeData(): Either<Failure, TradeHistoryDomainModel>? =
        tradeInMemoryCache.data

    override fun getTrade(tradeId: String): Either<Failure, TradeDomainModel> =
        tradeInMemoryCache.findTrade(tradeId)

    override fun getOrder(orderId: String): Either<Failure, OrderDomainModel> =
        tradeInMemoryCache.findOrder(orderId)

    override fun getFilterItem(): TradeFilterItem =
        tradeInMemoryCache.filterItem

    override suspend fun clearCache() {
        tradeInMemoryCache.clearCache()
    }

    override suspend fun updateFilter(filter: TradeFilter) {
        tradeInMemoryCache.updateFilter(filter, createTradeFilterItem(filter))
    }

    override suspend fun resetFilters() {
        val filter = createInitialFilter(
            tradeInMemoryCache.enabledCoins,
            tradeInMemoryCache.calculateDistance
        )
        tradeInMemoryCache.updateFilter(filter, createTradeFilterItem(filter))
    }

    override suspend fun fetchTrades(calculateDistance: Boolean) {
        val result = tradeApiService.loadTrades()
        val enabledCoins = accountDao.getAvailableAccounts()?.filter(AccountEntity::isEnabled).orEmpty()
        tradeInMemoryCache.initCoins(enabledCoins)
        val filter = createInitialFilter(tradeInMemoryCache.enabledCoins, calculateDistance)
        tradeInMemoryCache.updateFilter(filter, createTradeFilterItem(filter))
        tradeInMemoryCache.updateCache(calculateDistance, result)
    }

    override suspend fun sendLocation(location: Location) {
        tradeApiService.sendLocation(location)
    }

    override suspend fun createTrade(createTradeItem: CreateTradeItem, location: Location): Either<Failure, Unit> {
        val response = tradeApiService.createTrade(createTradeItem, location)
        return response.mapSuspend {
            tradeInMemoryCache.updateTrades(it)
        }
    }

    override suspend fun editTrade(editTrade: EditTradeItem): Either<Failure, Unit> {
        val response = tradeApiService.editTrade(editTrade)
        return response.mapSuspend {
            tradeInMemoryCache.updateTrades(it)
        }
    }

    override suspend fun cancelTrade(tradeId: String): Either<Failure, Unit> {
        val response = tradeApiService.cancelTrade(tradeId)
        return response.mapSuspend {
            tradeInMemoryCache.updateTrades(it)
        }
    }

    override suspend fun deleteTrade(tradeId: String): Either<Failure, Unit> {
        val response = tradeApiService.deleteTrade(tradeId)
        return response.mapSuspend {
            tradeInMemoryCache.updateTrades(it)
        }
    }

    override suspend fun createOrder(tradeOrder: TradeOrderItem, location: Location): Either<Failure, String> {
        val response = tradeApiService.createOrder(tradeOrder, location)
        return (response as Either.Right).b.id?.let { orderId ->
            response.mapSuspend {
                tradeInMemoryCache.updateOrders(it)
                orderId
            }
        } ?: response as Either.Left
    }

    override suspend fun updateOrder(status: UpdateOrderStatusItem): Either<Failure, Unit> {
        val response = tradeApiService.updateOrder(status.orderId, status = status.newStatus)
        return response.mapSuspend {
            tradeInMemoryCache.updateOrders(it)
        }
    }

    override suspend fun rateOrder(orderId: String, rate: Int): Either<Failure, Unit> {
        val response = tradeApiService.updateOrder(orderId, rate = rate)
        return response.mapSuspend {
            tradeInMemoryCache.updateOrders(it)
        }
    }

    override suspend fun cancelOrder(orderId: String): Either<Failure, Unit> =
        tradeApiService.deleteOrder(orderId).mapSuspend {
            tradeInMemoryCache.updateOrders(it)
        }

    private fun createInitialFilter(
        enabledCoins: List<AccountEntity>,
        calculateDistance: Boolean
    ): TradeFilter {
        val initialCoin = enabledCoins.find { it.type.name == LocalCoinType.BTC.name }?.type?.name
            ?: enabledCoins.firstOrNull()?.type?.name ?: ""
        return TradeFilter(
            initialCoin,
            getAvailablePaymentOptions(),
            calculateDistance,
            resources.getInteger(R.integer.trade_filter_min_distance),
            resources.getInteger(R.integer.trade_filter_max_distance),
            SortOption.PRICE
        )
    }

    private fun createTradeFilterItem(filter: TradeFilter): TradeFilterItem {
        return mapper.map(
            getAvailablePaymentOptions(),
            tradeInMemoryCache.enabledCoins,
            filter
        )
    }

}
