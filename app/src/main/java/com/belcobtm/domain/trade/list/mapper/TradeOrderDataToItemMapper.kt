package com.belcobtm.domain.trade.list.mapper

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.belcobtm.R
import com.belcobtm.data.helper.DistanceCalculator
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.order.OrderDomainModel
import com.belcobtm.domain.trade.model.order.OrderStatus
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.presentation.screens.wallet.trade.list.model.OrderItem
import com.belcobtm.presentation.screens.wallet.trade.list.model.OrderStatusItem
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem
import com.belcobtm.presentation.tools.formatter.Formatter

class TradeOrderDataToItemMapper(
    private val tradeItemMapper: TradeToTradeItemMapper,
    private val priceFormatter: Formatter<Double>,
    private val tradeCountFormatter: Formatter<Int>,
    private val distanceCalculator: DistanceCalculator,
    private val milesFormatter: Formatter<Double>,
) {

    fun map(order: OrderDomainModel?, tradeData: TradeHistoryDomainModel, myId: String): OrderItem? =
        with(order) {
            this ?: return null
            val cachedTrade = tradeData.trades[tradeId] ?: return@with null
            val trade = tradeItemMapper.map(cachedTrade)
            OrderItem(
                orderId = id,
                trade = trade,
                myTradeId = myId,
                mappedTradeType = resolveTradeType(this, trade, myId),
                coin = coin,
                orderStatus = OrderStatusItem(status, getStatusLabel(status), getStatusDrawable(status)),
                timestamp = timestamp,
                price = price,
                priceFormatted = priceFormatter.format(price),
                cryptoAmount = cryptoAmount,
                fiatAmount = fiatAmount,
                fiatAmountFormatted = priceFormatter.format(fiatAmount),
                paymentOptions = trade.paymentMethods,
                terms = terms,
                makerId = makerUserId,
                makerRate = makerRate,
                makerPublicId = makerUsername,
                makerLatitude = makerLatitude,
                makerLongitude = makerLongitude,
                makerTotalTrades = makerTradeTotal,
                makerTotalTradesFormatted = tradeCountFormatter.format(makerTradeTotal), makerTradingRate = makerTradeRate,
                takerId = takerUserId,
                takerRate = takerRate,
                takerPublicId = takerUsername,
                takerLatitude = takerLatitude,
                takerLongitude = takerLongitude,
                takerTotalTrades = takerTradeTotal,
                takerTotalTradesFormatted = tradeCountFormatter.format(takerTradeTotal),
                takerTradingRate = takerTradeRate,
                distanceFormatted = formatDistance()
            )
        }

    private fun OrderDomainModel.formatDistance(): String =
        if (takerLatitude > 0 && takerLongitude > 0 && makerLatitude > 0 && makerLongitude > 0)
            milesFormatter.format(
                distanceCalculator.calculateDistance(
                    takerLatitude, takerLongitude, makerLatitude, makerLongitude
                )
            ) else ""

    private fun resolveTradeType(order: OrderDomainModel, trade: TradeItem, myId: String): TradeType =
        when {
            order.makerUserId == myId -> trade.tradeType
            trade.tradeType == TradeType.BUY -> TradeType.SELL
            else -> TradeType.BUY
        }

    @DrawableRes
    private fun getStatusDrawable(status: OrderStatus): Int =
        when (status) {
            OrderStatus.NEW -> R.drawable.ic_order_status_new
            OrderStatus.CANCELED -> R.drawable.ic_order_status_canceled
            OrderStatus.DOING -> R.drawable.ic_order_status_doing
            OrderStatus.PAID -> R.drawable.ic_order_status_paid
            OrderStatus.RELEASED -> R.drawable.ic_order_status_released
            OrderStatus.DISPUTING -> R.drawable.ic_order_status_disputing
            OrderStatus.SOLVED -> R.drawable.ic_order_status_released
            else -> throw RuntimeException("Unknown trade type $status")
        }

    @StringRes
    private fun getStatusLabel(status: OrderStatus): Int =
        when (status) {
            OrderStatus.NEW -> R.string.order_status_new_label
            OrderStatus.CANCELED -> R.string.order_status_cancelled_label
            OrderStatus.DOING -> R.string.order_status_doing_label
            OrderStatus.PAID -> R.string.order_status_paid_label
            OrderStatus.RELEASED -> R.string.order_status_released_label
            OrderStatus.DISPUTING -> R.string.order_status_disputing_label
            OrderStatus.SOLVED -> R.string.order_status_solved_label
            else -> throw RuntimeException("Unknown order status $status")
        }

}
