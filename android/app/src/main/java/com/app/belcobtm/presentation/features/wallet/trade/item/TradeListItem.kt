package com.app.belcobtm.presentation.features.wallet.trade.item

sealed class TradeListItem(
    open val id: Int,
    open val userName: String,
    open val paymentMethod: String,
    open val price: String,
    open val priceLimit: String
) {

    data class Buy(
        override val id: Int,
        override val userName: String,
        override val paymentMethod: String,
        override val price: String,
        override val priceLimit: String
    ) : TradeListItem(id, userName, paymentMethod, price, priceLimit)
}