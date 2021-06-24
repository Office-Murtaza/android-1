package com.app.belcobtm.data.websockets.base.model

data class StompSocketResponse(
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