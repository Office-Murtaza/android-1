package com.app.belcobtm.presentation.features.wallet.trade.list.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class TradeStatistics(
    val publicId: String,
    @DrawableRes val statusIcon: Int,
    @StringRes val statusLabel: Int,
    val totalTrades: Int,
    val tradingRate: Double
)