package com.app.belcobtm.domain.trade.list.mapper

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.app.belcobtm.R
import com.app.belcobtm.data.helper.DistanceCalculator
import com.app.belcobtm.data.model.trade.Order
import com.app.belcobtm.data.model.trade.OrderStatus
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderItem
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderStatusItem
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class TradeOrderDataToItemMapper(
    private val tradeItemMapper: TradeToTradeItemMapper,
    private val priceFormatter: Formatter<Double>,
    private val tradeCountFormatter: Formatter<Int>,
    private val statusMapper: TraderStatusToIconMapper,
    private val distanceCalculator: DistanceCalculator,
    private val milesFormatter: Formatter<Double>,
) {

    fun map(order: Order, tradeData: TradeData, myId: String): OrderItem? =
        with(order) {
            val cachedTrade = tradeData.trades[tradeId] ?: return@with null
            val trade = tradeItemMapper.map(cachedTrade)
            OrderItem(
                id, trade, myId, resolveTradeType(order, trade, myId), LocalCoinType.valueOf(coinCode),
                OrderStatusItem(status, getStatusLabel(status), getStatusDrawable(status)),
                timestamp, price, priceFormatter.format(price),
                cryptoAmount, fiatAmount, priceFormatter.format(fiatAmount),
                trade.paymentMethods, terms, makerId, makerStatusId, statusMapper.map(makerStatusId),
                makerPublicId, makerLatitude, makerLongitude, makerTotalTrades,
                tradeCountFormatter.format(makerTotalTrades), makerTradingRate,
                takerId, takerStatusId, statusMapper.map(takerStatusId), takerPublicId,
                takerLatitude, takerLongitude, takerTotalTrades,
                tradeCountFormatter.format(takerTotalTrades), takerTradingRate,
                formatDistance()
            )
        }

    private fun Order.formatDistance(): String?  {
        takerLatitude ?: return null
        takerLongitude ?: return null
        makerLatitude ?: return null
        makerLongitude ?: return null
        return milesFormatter.format(
            distanceCalculator.calculateDistance(
                takerLatitude, takerLongitude, makerLatitude, makerLongitude
            )
        )
    }


    private fun resolveTradeType(order: Order, trade: TradeItem, myId: String): Int =
        when {
            order.makerId == myId -> trade.tradeType
            trade.tradeType == TradeType.BUY -> TradeType.SELL
            else -> TradeType.BUY
        }

    @DrawableRes
    private fun getStatusDrawable(@OrderStatus status: Int): Int =
        when (status) {
            OrderStatus.NEW -> R.drawable.ic_order_status_new
            OrderStatus.CANCELLED -> R.drawable.ic_order_status_canceled
            OrderStatus.DOING -> R.drawable.ic_order_status_doing
            OrderStatus.PAID -> R.drawable.ic_order_status_paid
            OrderStatus.RELEASED -> R.drawable.ic_order_status_released
            OrderStatus.DISPUTING -> R.drawable.ic_order_status_disputing
            OrderStatus.SOLVED -> R.drawable.ic_order_status_released
            else -> throw RuntimeException("Unknown trade type $status")
        }

    @StringRes
    private fun getStatusLabel(@OrderStatus status: Int): Int =
        when (status) {
            OrderStatus.NEW -> R.string.order_status_new_label
            OrderStatus.CANCELLED -> R.string.order_status_cancelled_label
            OrderStatus.DOING -> R.string.order_status_doing_label
            OrderStatus.PAID -> R.string.order_status_paid_label
            OrderStatus.RELEASED -> R.string.order_status_released_label
            OrderStatus.DISPUTING -> R.string.order_status_disputing_label
            OrderStatus.SOLVED -> R.string.order_status_solved_label
            else -> throw RuntimeException("Unknown trade type $status")
        }
}