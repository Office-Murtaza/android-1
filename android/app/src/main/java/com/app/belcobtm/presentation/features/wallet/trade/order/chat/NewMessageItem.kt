package com.app.belcobtm.presentation.features.wallet.trade.order.chat

import android.graphics.Bitmap

data class NewMessageItem(
    val orderId: String,
    val fromId: String,
    val toId: String,
    val content: String,
    val attachmentName: String? = null,
    val attachment: Bitmap? = null
)