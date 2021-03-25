package com.app.belcobtm.data.websockets.chat.serializer

import android.graphics.Bitmap
import android.util.Base64
import com.app.belcobtm.data.websockets.chat.model.SendMessageRequest
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem
import com.squareup.moshi.Moshi
import java.io.ByteArrayOutputStream

class ChatRequestSerializer(
    private val moshi: Moshi
) : RequestSerializer<NewMessageItem> {

    companion object {
        const val CHAT_REQUEST_SERIALIZER_QUALIFIER = "ChatRequestSerializer"
        const val CONVERTED_BITMAP_FORMAT = "png"
    }

    override fun serialize(request: NewMessageItem): String =
        with(request) {
            SendMessageRequest(
                orderId,
                fromId,
                toId,
                content,
                request.attachment?.let(::convertBitmapToBase64),
                request.attachment?.let { CONVERTED_BITMAP_FORMAT }
            )
        }.let {
            moshi.adapter(SendMessageRequest::class.java)
                .toJson(it)
        }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        bitmap.recycle()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}