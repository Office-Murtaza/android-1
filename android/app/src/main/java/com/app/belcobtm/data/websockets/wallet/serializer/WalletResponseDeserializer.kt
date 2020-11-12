package com.app.belcobtm.data.websockets.wallet.serializer

import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.data.websockets.wallet.model.WalletSocketResponse

class WalletResponseDeserializer : ResponseDeserializer<WalletSocketResponse> {

    override fun deserialize(content: String): WalletSocketResponse {
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
        return WalletSocketResponse(
            response.first(), headers, response.last().replace("\u0000", "")
        )
    }
}