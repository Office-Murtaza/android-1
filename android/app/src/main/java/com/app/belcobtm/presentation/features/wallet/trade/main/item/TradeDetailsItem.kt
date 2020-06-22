package com.app.belcobtm.presentation.features.wallet.trade.main.item

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class TradeDetailsItem {

    @Parcelize
    data class BuySell(
        val id: Int,
        val minLimit: Int,
        val maxLimit: Int,
        val tradeCount: Int,
        val distance: Int,
        val rate: Double,
        val userName: String,
        val paymentMethod: String,
        val price: Double,
        val terms: String,
        val isBuyType: Boolean
    ) : TradeDetailsItem(), Parcelable

    @Parcelize
    data class My(
        val id: Int,
        val minLimit: Int,
        val maxLimit: Int,
        val tradeCount: Int,
        val distance: Int,
        val rate: Double,
        val userName: String,
        val paymentMethod: String,
        val price: Double,
        val isBuyType: Boolean
    ) : TradeDetailsItem(), Parcelable

    @Parcelize
    data class Open(
        val id: Int,
        val minLimit: Int,
        val maxLimit: Int,
        val tradeCount: Int,
        val distance: Int,
        val rate: Double,
        val userName: String,
        val paymentMethod: String,
        val price: Double,
        val isBuyType: Boolean
    ) : TradeDetailsItem(), Parcelable

    object Empty : TradeDetailsItem()
    object Loading : TradeDetailsItem()
    object Error : TradeDetailsItem()
}
