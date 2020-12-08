package com.app.belcobtm.data.websockets.base.model

import com.app.belcobtm.domain.Either

sealed class SocketResponse {

    object Opened : SocketResponse()

    object Disconnected : SocketResponse()

    class Failure(val cause: Throwable) : SocketResponse()

    data class Message(val content: Either<String, Throwable>) : SocketResponse()
}