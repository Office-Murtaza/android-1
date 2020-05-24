package com.app.belcobtm.presentation.features.wallet.trade.main.item

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class TradeDetailsItem(
    open val id: Int,
    open val minLimit: Int,
    open val maxLimit: Int,
    open val tradeCount: Int,
    open val distance: Int,
    open val rate: Double,
    open val userName: String,
    open val paymentMethod: String,
    open val price: Double
) {

    @Parcelize
    data class Buy(
        override val id: Int,
        override val minLimit: Int,
        override val maxLimit: Int,
        override val tradeCount: Int,
        override val distance: Int,
        override val rate: Double,
        override val userName: String,
        override val paymentMethod: String,
        override val price: Double,
        val terms: String
    ) : TradeDetailsItem(id, minLimit, maxLimit, tradeCount, distance, rate, userName, paymentMethod, price), Parcelable

    @Parcelize
    data class Sell(
        override val id: Int,
        override val minLimit: Int,
        override val maxLimit: Int,
        override val tradeCount: Int,
        override val distance: Int,
        override val rate: Double,
        override val userName: String,
        override val paymentMethod: String,
        override val price: Double
    ) : TradeDetailsItem(id, minLimit, maxLimit, tradeCount, distance, rate, userName, paymentMethod, price), Parcelable

    @Parcelize
    data class Open(
        override val id: Int,
        override val minLimit: Int,
        override val maxLimit: Int,
        override val tradeCount: Int,
        override val distance: Int,
        override val rate: Double,
        override val userName: String,
        override val paymentMethod: String,
        override val price: Double,
        val isBuyType: Boolean
    ) : TradeDetailsItem(id, minLimit, maxLimit, tradeCount, distance, rate, userName, paymentMethod, price), Parcelable

    object Empty : TradeDetailsItem(-1, 0, 0, 0, 0, 0.0, "", "", 0.0)
}