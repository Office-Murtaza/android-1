package com.app.belcobtm.data.sockets

interface SocketMessageListener {
    fun onMessage(message: SocketMessage)
}