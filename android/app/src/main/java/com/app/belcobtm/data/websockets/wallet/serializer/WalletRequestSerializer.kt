package com.app.belcobtm.data.websockets.wallet.serializer

import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.serializer.RequestSerializer

class WalletRequestSerializer : RequestSerializer<StompSocketRequest> {

    override fun serialize(request: StompSocketRequest): String =
        StringBuilder().apply {
            append("${request.command}\n")
            request.headers.forEach { (header, value) ->
                append(header)
                append(":")
                append(value)
                append("\n")
            }
            append("\n")
            append(request.body)
            append('\u0000')
        }.toString()
}