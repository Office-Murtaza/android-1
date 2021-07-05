package com.belcobtm.data.websockets.base.serializer

import com.belcobtm.data.websockets.base.model.StompSocketResponse
import com.belcobtm.data.websockets.serializer.ResponseDeserializer

class StompResponseDeserializer : ResponseDeserializer<StompSocketResponse> {

    companion object {
        const val STOMP_RESPONSE_DESERIALIZER_QUALIFIER = "StompResponseDeserializer"
    }

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