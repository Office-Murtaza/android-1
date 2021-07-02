package com.app.belcobtm.data.websockets.chat.serializer

import com.app.belcobtm.data.websockets.chat.model.SendMessageRequest
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem
import com.squareup.moshi.Moshi

class ChatRequestSerializer(
    private val moshi: Moshi
) : RequestSerializer<NewMessageItem> {

    companion object {
        const val CHAT_REQUEST_SERIALIZER_QUALIFIER = "ChatRequestSerializer"
    }

    override fun serialize(request: NewMessageItem): String =
        with(request) {
            SendMessageRequest(
                orderId, fromId, toId, content,
                System.currentTimeMillis(),
                request.attachmentName
            )
        }.let {
            moshi.adapter(SendMessageRequest::class.java)
                .toJson(it)
        }
}