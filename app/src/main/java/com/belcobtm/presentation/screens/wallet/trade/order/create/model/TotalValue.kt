package com.belcobtm.presentation.screens.wallet.trade.order.create.model

import androidx.annotation.StringRes

data class TotalValue(
    val totalValueCrypto: Double,
    val coinName: String,
    @StringRes val labelId: Int
)