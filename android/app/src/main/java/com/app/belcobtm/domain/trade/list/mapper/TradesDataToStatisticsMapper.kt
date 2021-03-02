package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeStatistics

class TradesDataToStatisticsMapper {

    fun map(tradeData: TradeData): TradeStatistics =
        with(tradeData.statistics) {
            TradeStatistics(
                publicId,
                status,
                totalTrades,
                tradingRate
            )
        }

}