package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class TradeToTradeItemMapper(
    private val paymentOptionMapper: TradePaymentOptionMapper,
    private val milesFormatter: Formatter<Double>,
    private val statusMapper: TraderStatusToIconMapper
) {

    fun map(trade: Trade): TradeItem =
        with(trade) {
            TradeItem(
                id, type, LocalCoinType.valueOf(coinCode),
                status, createDate, price, minLimit, maxLimit,
                ordersCount, paymentMethods.map(paymentOptionMapper::map),
                terms, makerId, statusMapper.map(makerStatus),
                makerPublicId, makerTotalTrades, makerTradingRate,
                distance, milesFormatter.format(distance)
            )
        }
}