package com.app.belcobtm.presentation.features.wallet.trade.main.item

import android.os.Parcelable
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem.Companion.TRADE_DETAILS_ITEM_TYPE
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TradeDetailsBuySellItem(
    override val tradeId: Int,
    override val minLimit: Int,
    override val maxLimit: Int,
    override val tradeCount: Int,
    override val distance: Int,
    override val rate: Double,
    override val userName: String,
    override val paymentMethod: String,
    override val price: Double,
    val terms: String,
    override val isBuyType: Boolean
) : TradeDetailsItem, Parcelable {

    override val id: String
        get() = tradeId.toString()

    override val type: Int
        get() = TRADE_DETAILS_ITEM_TYPE
}