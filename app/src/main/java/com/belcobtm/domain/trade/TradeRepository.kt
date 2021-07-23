package com.belcobtm.domain.trade

import android.location.Location
import com.belcobtm.data.model.trade.PaymentOption
import com.belcobtm.data.model.trade.Trade
import com.belcobtm.data.model.trade.TradeData
import com.belcobtm.data.model.trade.filter.TradeFilter
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem
import com.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem
import com.belcobtm.presentation.features.wallet.trade.list.filter.model.TradeFilterItem
import com.belcobtm.presentation.features.wallet.trade.order.create.model.TradeOrderItem
import com.belcobtm.presentation.features.wallet.trade.order.details.model.UpdateOrderStatusItem
import kotlinx.coroutines.flow.Flow

interface TradeRepository {

    fun getAvailablePaymentOptions(): List<@PaymentOption Int>

    fun observeTradeData(): Flow<Either<Failure, TradeData>?>

    fun observeFilter(): Flow<TradeFilter?>

    fun observeLastSeenMessageTimestamp(): Flow<Long>

    suspend fun updateLastSeenMessageTimestamp()

    fun getTradeData(): Either<Failure, TradeData>?

    fun getTrade(tradeId: String): Either<Failure, Trade>

    fun getFilterItem(): TradeFilterItem

    suspend fun clearCache()

    suspend fun updateFilter(filter: TradeFilter)

    suspend fun resetFilters()

    suspend fun fetchTrades(calculateDistance: Boolean)

    suspend fun sendLocation(location: Location)

    suspend fun createTrade(createTradeItem: CreateTradeItem): Either<Failure, Unit>

    suspend fun editTrade(editTrade: EditTradeItem): Either<Failure, Unit>

    suspend fun cancelTrade(tradeId: String): Either<Failure, Unit>

    suspend fun cancelOrder(orderId: String): Either<Failure, Unit>

    suspend fun createOrder(tradeOrder: TradeOrderItem): Either<Failure, String>

    suspend fun updateOrder(status: UpdateOrderStatusItem): Either<Failure, Unit>

    suspend fun rateOrder(orderId: String, rate: Int): Either<Failure, Unit>
}