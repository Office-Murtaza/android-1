package com.app.belcobtm.data.websockets.wallet.serializer

import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.wallet.model.WalletSocketRequest

class WalletRequestSerializer : RequestSerializer<WalletSocketRequest> {

    override fun serialize(request: WalletSocketRequest): String =
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