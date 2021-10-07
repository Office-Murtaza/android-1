package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.R
import com.belcobtm.data.model.trade.TradeData
import com.belcobtm.data.model.trade.TraderStatus
import com.belcobtm.presentation.features.wallet.trade.list.model.TradeStatistics

class TradesDataToStatisticsMapper(
    private val statusMapper: TraderStatusToIconMapper
) {

    fun map(tradeData: TradeData): TradeStatistics =
        with(tradeData.statistics) {
            TradeStatistics(
                publicId.orEmpty(),
                statusMapper.map(status),
                getStatusLabel(status),
                totalTrades,
                tradingRate
            )
        }

    private fun getStatusLabel(@TraderStatus status: Int): Int =
        when (status) {
            TraderStatus.NOT_VERIFIED -> R.string.user_trade_info_not_verified_status_label
            TraderStatus.VERIFICATION_PENDING -> R.string.user_trade_info_not_verified_status_label
            TraderStatus.VERIFICATION_REJECTED -> R.string.user_trade_info_not_verified_status_label
            TraderStatus.VERIFIED -> R.string.user_trade_info_verified_status_label
            TraderStatus.VIP_VERIFIED -> R.string.user_trade_info_vip_verified_status_label
            TraderStatus.VIP_VERIFICATION_PENDING -> R.string.user_trade_info_not_verified_status_label
            TraderStatus.VIP_VERIFICATION_REJECTED -> R.string.user_trade_info_not_verified_status_label
            else -> throw RuntimeException("Unknown status $status")
        }

}