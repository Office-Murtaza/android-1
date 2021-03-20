package com.app.belcobtm.domain.trade.list.mapper

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.Order
import com.app.belcobtm.data.model.trade.OrderStatus
import com.app.belcobtm.data.model.trade.PaymentOption
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderItem

class TradesDataToOrderListMapper(
    private val paymentOptionMapper: TradePaymentOptionMapper
) {

    fun map(tradeData: TradeData): List<OrderItem> =
        tradeData.orders.map(::mapOrder)

    private fun mapOrder(trade: Order): OrderItem =
        with(trade) {
            OrderItem(
                id, type, type, LocalCoinType.valueOf(coinCode),
                getStatusLabel(status), getStatusDrawable(status),
                timestamp, price, cryptoAmount, fiatAmount,
                listOf(paymentOptionMapper.map(PaymentOption.VENMO)),
                terms, makerId, makerPublicId, makerLatitude,
                makerLongitude, makerTotalTrades, makerTradingRate,
                takerId, takerPublicId, takerLatitude, takerLongitude,
                takerTotalTrades, takerTradingRate
            )
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
            OrderStatus.SOLVED -> R.drawable.ic_order_status_released // TODO update icon
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