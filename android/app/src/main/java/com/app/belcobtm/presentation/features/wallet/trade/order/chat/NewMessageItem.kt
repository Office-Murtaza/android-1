package com.app.belcobtm.presentation.features.wallet.trade.order.chat

import android.graphics.Bitmap

data class NewMessageItem(
    val orderId: String,
    val fromId: Int,
    val toId: Int,
    val content: String,
    val attachmentName: String? = null,
    val attachment: Bitmap? = null
)