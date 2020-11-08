package com.app.belcobtm.data.websockets.wallet.model

class WalletSocketResponse(
    val status: String,
    val headers: Map<String, String>,
    val body: String
) {
    companion object {
        const val CONNECTED = "CONNECTED"
        const val CONTENT = "MESSAGE"
        const val ERROR = "ERROR"
    }
}