package com.app.belcobtm.data.websockets.chat.model

data class ChatMessageResponse(
    val orderId: Int,
    val fromUserId: Int,
    val toUserId: Int,
    val message: String,
    val fileBase64: String?,
    val fileExtension: String?,
    val timestamp: Long
)