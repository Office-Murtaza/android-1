package com.app.belcobtm.data.websockets.chat.model

data class ChatMessageResponse(
    val orderId: String,
    val fromUserId: Int,
    val toUserId: Int,
    val message: String,
    val file: String?,
    val timestamp: Long
)