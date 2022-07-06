package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.domain.trade.model.trade.TradeDomainModel
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem
import com.belcobtm.presentation.tools.formatter.Formatter

class TradeToTradeItemMapper(
    private val paymentOptionMapper: TradePaymentOptionMapper,
    private val milesFormatter: Formatter<Double>,
    private val priceFormatter: Formatter<Double>,
    private val tradeCountFormatter: Formatter<Int>
) {

    fun map(trade: TradeDomainModel): TradeItem =
        with(trade) {
            TradeItem(
                tradeId = id,
                tradeType = type,
                coin = coin,
                status = status,
                price = price,
                timestamp = timestamp,
                priceFormatted = priceFormatter.format(price),
                minLimit = minLimit,
                minLimitFormatted = priceFormatter.format(minLimit),
                maxLimit = maxLimit,
                maxLimitFormatted = priceFormatter.format(maxLimit),
                ordersCount = ordersCount,
                paymentMethods = paymentMethods.map(paymentOptionMapper::map),
                terms = terms,
                makerId = makerId,
                makerPublicId = makerUsername,
                makerTotalTrades = makerTotalTrades,
                makerTotalTradesFormatted = tradeCountFormatter.format(makerTotalTrades),
                makerTradingRate = makerTradingRate,
                makerLatitude = makerLatitude,
                makerLongitude = makerLongitude,
                distance = distance,
                distanceFormatted = milesFormatter.format(distance)
            )
        }

}
