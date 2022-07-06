package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeStatistics

class TradesDataToStatisticsMapper {

    fun map(tradeData: TradeHistoryDomainModel): TradeStatistics =
        with(tradeData.statistics) {
            TradeStatistics(
                publicId,
                totalTrades,
                tradingRate
            )
        }

}
