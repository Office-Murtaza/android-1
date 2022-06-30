package com.belcobtm.presentation.screens.wallet.trade.order.chat

data class NewMessageItem(
    val orderId: String,
    val fromId: String,
    val toId: String,
    val content: String,
    val attachmentName: String? = null
)