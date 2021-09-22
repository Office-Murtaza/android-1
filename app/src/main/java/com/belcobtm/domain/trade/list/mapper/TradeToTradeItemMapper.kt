package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.data.model.trade.Trade
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class TradeToTradeItemMapper(
    private val paymentOptionMapper: TradePaymentOptionMapper,
    private val milesFormatter: Formatter<Double>,
    private val priceFormatter: Formatter<Double>,
    private val tradeCountFormatter: Formatter<Int>,
    private val statusMapper: TraderStatusToIconMapper
) {

    fun map(trade: Trade): TradeItem =
        with(trade) {
            TradeItem(
                id, type, LocalCoinType.valueOf(coinCode),
                status, price, timestamp, priceFormatter.format(price),
                minLimit, priceFormatter.format(minLimit),
                maxLimit, priceFormatter.format(maxLimit),
                ordersCount, paymentMethods.map(paymentOptionMapper::map),
                terms, makerId, statusMapper.map(makerStatus),
                makerUsername, makerTotalTrades, tradeCountFormatter.format(makerTotalTrades),
                makerTradingRate, makerLatitude, makerLongitude, distance,
                milesFormatter.format(distance)
            )
        }
}