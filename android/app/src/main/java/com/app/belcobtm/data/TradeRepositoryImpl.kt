package com.app.belcobtm.data

import android.content.res.Resources
import com.app.belcobtm.R
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.inmemory.TradeInMemoryCache
import com.app.belcobtm.data.model.trade.PaymentOption
import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.filter.SortOption
import com.app.belcobtm.data.model.trade.filter.TradeFilter
import com.app.belcobtm.data.provider.location.LocationProvider
import com.app.belcobtm.data.rest.trade.TradeApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem
import com.app.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem
import kotlinx.coroutines.flow.Flow

class TradeRepositoryImpl(
    private val tradeApiService: TradeApiService,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val accountDao: AccountDao,
    private val resources: Resources,
    private val locationProvider: LocationProvider
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

    override fun getTradeData(): Either<Failure, TradeData>? =
        tradeInMemoryCache.data

    override fun getTrade(tradeId: Int): Either<Failure, Trade> =
        tradeInMemoryCache.findTrade(tradeId)

    override fun getFilter(): TradeFilter? =
        tradeInMemoryCache.filter

    override suspend fun updateFilter(filter: TradeFilter) {
        tradeInMemoryCache.updateFilter(filter)
    }

    override suspend fun resetFilters() {
        tradeInMemoryCache.updateFilter(createInitialFilter(tradeInMemoryCache.calculateDistance))
    }

    override suspend fun fetchTrades(calculateDistance: Boolean) {
        val result = tradeApiService.loadTrades()
        tradeInMemoryCache.updateFilter(createInitialFilter(calculateDistance))
        tradeInMemoryCache.updateCache(calculateDistance, result)
    }

    override suspend fun createTrade(createTradeItem: CreateTradeItem): Either<Failure, Unit> {
        val location = locationProvider.getCurrentLocation()
        val response = tradeApiService.createTrade(createTradeItem, location)
        return if (response.isRight) {
            Either.Right(Unit)
        } else {
            response as Either.Left
        }
    }

    override suspend fun editTrade(editTrade: EditTradeItem): Either<Failure, Unit> {
        val response = tradeApiService.editTrade(editTrade)
        return if (response.isRight) {
            Either.Right(Unit)
        } else {
            response as Either.Left
        }
    }

    override suspend fun deleteTrade(tradeId: Int): Either<Failure, Unit> {
        val response = tradeApiService.deleteTrade(tradeId)
        return if (response.isRight) {
            Either.Right(Unit)
        } else {
            response as Either.Left
        }
    }

    override suspend fun createOrder(): Either<Failure, Unit> {
        TODO("Implement trade creation")
    }

    private suspend fun createInitialFilter(calculateDistance: Boolean): TradeFilter {
        val enabledCoins = accountDao.getItemList()
            .orEmpty()
            .map { it.type.name }
        val initialCoin = enabledCoins.find { it == LocalCoinType.BTC.name } ?: enabledCoins.firstOrNull() ?: ""
        return TradeFilter(
            initialCoin,
            getAvailablePaymentOptions(),
            calculateDistance,
            resources.getInteger(R.integer.trade_filter_min_distance),
            resources.getInteger(R.integer.trade_filter_max_distance),
            SortOption.PRICE
        )
    }
}