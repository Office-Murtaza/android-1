package com.app.belcobtm.data.websockets.wallet.serializer

import com.app.belcobtm.data.websockets.base.model.StompSocketResponse
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer

class WalletResponseDeserializer : ResponseDeserializer<StompSocketResponse> {

    override fun deserialize(content: String): StompSocketResponse {
        val response = content.split("\n")
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toList()
        val headers = HashMap<String, String>()
        for (i in 1 until response.size - 1) {
            val (header, value) = response[i].split(":")
            headers[header] = value
        }
        return StompSocketResponse(
            response.first(), headers, response.last().replace("\u0000", "")
        )
    }
}