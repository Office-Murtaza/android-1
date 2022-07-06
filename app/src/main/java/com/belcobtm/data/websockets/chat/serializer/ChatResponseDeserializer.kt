package com.belcobtm.data.websockets.chat.serializer

import com.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.squareup.moshi.Moshi

class ChatResponseDeserializer(private val moshi: Moshi) : ResponseDeserializer<ChatMessageResponse?> {

    companion object {

        const val CHAT_RESPONSE_DESERIALIZER_QUALIFIER = "ChatResponseDeserializer"
    }

    override fun deserialize(content: String): ChatMessageResponse? =
        moshi.adapter(ChatMessageResponse::class.java)
            .fromJson(content)

}
