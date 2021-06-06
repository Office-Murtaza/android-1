package com.app.belcobtm.data

import android.content.res.Resources
import android.location.Location
import com.app.belcobtm.R
import com.app.belcobtm.data.disk.database.account.AccountDao
import com.app.belcobtm.data.disk.database.account.AccountEntity
import com.app.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.app.belcobtm.data.model.trade.PaymentOption
import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.filter.SortOption
import com.app.belcobtm.data.model.trade.filter.TradeFilter
import com.app.belcobtm.data.provider.location.LocationProvider
import com.app.belcobtm.data.rest.trade.TradeApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.filter.mapper.TradeFilterItemMapper
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem
import com.app.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.TradeFilterItem
import com.app.belcobtm.presentation.features.wallet.trade.order.create.model.TradeOrderItem
import com.app.belcobtm.presentation.features.wallet.trade.order.details.model.UpdateOrderStatusItem
import kotlinx.coroutines.flow.Flow

class TradeRepositoryImpl(
    private val tradeApiService: TradeApiService,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val accountDao: AccountDao,
    private val resources: Resources,
    private val locationProvider: LocationProvider,
    private val mapper: TradeFilterItemMapper
) : TradeRepository {

    override fun getAvailablePaymentOptions(): List<@PaymentOption Int> =
        listOf(
            PaymentOption.CASH,
            PaymentOption.PAYPAL,
            PaymentOption.VENMO,
            PaymentOption.CASH_APP,
            PaymentOption.PAYONEER
        )

    override fun observeTradeData(): Flow<Either<Failure, TradeData>?> =
        tradeInMemoryCache.observableData

    override fun observeFilter(): Flow<TradeFilter?> =
        tradeInMemoryCache.observableFilter

    override fun observeLastSeenMessageTimestamp(): Flow<Long> =
        tradeInMemoryCache.observableLastSeenMessageTimestamp

    override fun updateLastSeenMessageTimestamp() {
        tradeInMemoryCache.updateLastSeenMessageTimestamp()
    }

    override fun getTradeData(): Either<Failure, TradeData>? =
        tradeInMemoryCache.data

    override fun getTrade(tradeId: String): Either<Failure, Trade> =
        tradeInMemoryCache.findTrade(tradeId)

    override fun getFilterItem(): TradeFilterItem =
        tradeInMemoryCache.filterItem

    override fun clearCache() {
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
        val enabledCoins = accountDao.getItemList().orEmpty()
        tradeInMemoryCache.initCoins(enabledCoins)
        val filter = createInitialFilter(tradeInMemoryCache.enabledCoins, calculateDistance)
        tradeInMemoryCache.updateFilter(filter, createTradeFilterItem(filter))
        tradeInMemoryCache.updateCache(calculateDistance, result)
    }

    override suspend fun sendLocation(location: Location) {
        tradeApiService.sendLocation(location)
    }

    override suspend fun createTrade(createTradeItem: CreateTradeItem): Either<Failure, Unit> {
        val location = locationProvider.getCurrentLocation()
        val response = tradeApiService.createTrade(createTradeItem, location)
        return response.map { Unit }
    }

    override suspend fun editTrade(editTrade: EditTradeItem): Either<Failure, Unit> {
        val response = tradeApiService.editTrade(editTrade)
        return response.map { Unit }
    }

    override suspend fun cancelTrade(tradeId: String): Either<Failure, Unit> {
        val response = tradeApiService.deleteTrade(tradeId)
        return response.map { Unit }
    }

    override suspend fun createOrder(tradeOrder: TradeOrderItem): Either<Failure, String> {
        val response = tradeApiService.createOrder(tradeOrder)
        return response.map {
            tradeInMemoryCache.updateOrders(it)
            it.id
        }
    }

    override suspend fun updateOrder(status: UpdateOrderStatusItem): Either<Failure, Unit> {
        val response = tradeApiService.updateOrder(status.orderId, status = status.newStatus)
        return response.map { tradeInMemoryCache.updateOrders(it) }
    }

    override suspend fun rateOrder(orderId: String, rate: Int): Either<Failure, Unit> {
        val response = tradeApiService.updateOrder(orderId, rate = rate)
        return response.map { tradeInMemoryCache.updateOrders(it) }
    }

    private fun createInitialFilter(enabledCoins: List<AccountEntity>, calculateDistance: Boolean): TradeFilter {
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