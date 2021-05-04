package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TraderStatus

class TraderStatusToIconMapper {

    fun map(@TraderStatus status: Int) =
        when (status) {
            TraderStatus.NOT_VERIFIED -> R.drawable.ic_trade_maker_not_verified
            TraderStatus.VERIFIED -> R.drawable.ic_trade_maker_verified
            TraderStatus.VIP_VERIFIED -> R.drawable.ic_trade_maker_vip_verified
            else -> R.drawable.ic_trade_maker_not_verified
        }
}