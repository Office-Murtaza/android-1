package com.app.belcobtm.presentation.features.wallet.trade.main.item

data class TradePageItem<T : TradeDetailsItem>(
    val itemList: List<T>
)