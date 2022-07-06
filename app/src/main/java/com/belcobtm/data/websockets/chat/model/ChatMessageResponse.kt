package com.belcobtm.data.websockets.chat.model

data class ChatMessageResponse(
    val orderId: String?,
    val fromUserId: String?,
    val toUserId: String?,
    val message: String?,
    val file: String?,
    val timestamp: Long?
)
