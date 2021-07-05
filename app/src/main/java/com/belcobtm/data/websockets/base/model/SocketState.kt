package com.belcobtm.data.websockets.base.model

sealed class SocketState {
    object Connected : SocketState()
    object None : SocketState()
    object Closed : SocketState()
}