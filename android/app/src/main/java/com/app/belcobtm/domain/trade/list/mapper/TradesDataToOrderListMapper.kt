package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.data.model.trade.Order
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
                id, type, LocalCoinType.valueOf(coinCode),
                status, createDate, price, cryptoAmount, fiatAmount,
                paymentMethods.map(paymentOptionMapper::map),
                terms, makerId, makerPublicId, makerLatitude,
                makerLongitude, makerTotalTrades, makerTradingRate,
                takerId, takerPublicId, takerLatitude, takerLongitude,
                takerTotalTrades, takerTradingRate
            )
        }
}