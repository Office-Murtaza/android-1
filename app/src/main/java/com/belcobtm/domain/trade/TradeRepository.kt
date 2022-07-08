package com.belcobtm.domain.trade

import android.location.Location
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.filter.TradeFilter
import com.belcobtm.domain.trade.model.order.OrderDomainModel
import com.belcobtm.domain.trade.model.trade.TradeDomainModel
import com.belcobtm.presentation.screens.wallet.trade.create.model.CreateTradeItem
import com.belcobtm.presentation.screens.wallet.trade.edit.EditTradeItem
import com.belcobtm.presentation.screens.wallet.trade.list.filter.model.TradeFilterItem
import com.belcobtm.presentation.screens.wallet.trade.order.create.model.TradeOrderItem
import com.belcobtm.presentation.screens.wallet.trade.order.details.model.UpdateOrderStatusItem
import kotlinx.coroutines.flow.Flow

interface TradeRepository {

    fun getAvailablePaymentOptions(): List<PaymentMethodType>

    fun observeTradeData(): Flow<Either<Failure, TradeHistoryDomainModel>?>

    fun observeFilter(): Flow<TradeFilter?>

    fun observeLastSeenMessageTimestamp(): Flow<Long>

    suspend fun updateLastSeenMessageTimestamp()

    fun getTradeData(): Either<Failure, TradeHistoryDomainModel>?

    fun getTrade(tradeId: String): Either<Failure, TradeDomainModel>

    fun getOrder(orderId: String): Either<Failure, OrderDomainModel>

    fun getFilterItem(): TradeFilterItem

    suspend fun clearCache()

    suspend fun updateFilter(filter: TradeFilter)

    suspend fun resetFilters()

    suspend fun fetchTrades(calculateDistance: Boolean)

    suspend fun sendLocation(location: Location)

    suspend fun createTrade(createTradeItem: CreateTradeItem, location: Location): Either<Failure, Unit>

    suspend fun editTrade(editTrade: EditTradeItem): Either<Failure, Unit>

    suspend fun cancelTrade(tradeId: String): Either<Failure, Unit>

    suspend fun deleteTrade(tradeId: String): Either<Failure, Unit>

    suspend fun createOrder(tradeOrder: TradeOrderItem, location: Location): Either<Failure, String>

    suspend fun updateOrder(status: UpdateOrderStatusItem): Either<Failure, Unit>

    suspend fun rateOrder(orderId: String, rate: Int): Either<Failure, Unit>

    suspend fun cancelOrder(orderId: String): Either<Failure, Unit>

}
