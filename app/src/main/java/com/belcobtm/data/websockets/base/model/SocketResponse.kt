package com.belcobtm.data.websockets.base.model

sealed class SocketResponse {

    object Opened : SocketResponse()

    object Disconnected : SocketResponse()

    class Failure(val cause: Throwable) : SocketResponse()

    data class Message(val content: String) : SocketResponse()
}