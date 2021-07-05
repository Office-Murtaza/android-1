package com.belcobtm.data.websockets.chat.model

data class SendMessageRequest(
    val orderId: String,
    val fromUserId: String,
    val toUserId: String,
    val message: String,
    val timestamp: Long,
    val file: String?
)