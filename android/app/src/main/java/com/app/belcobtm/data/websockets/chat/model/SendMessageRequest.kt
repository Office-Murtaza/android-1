package com.app.belcobtm.data.websockets.chat.model

data class SendMessageRequest(
    val orderId: String,
    val fromUserId: Int,
    val toUserId: Int,
    val message: String,
    val timestamp: Long,
    val file: String?
)