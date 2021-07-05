package com.belcobtm.data.websockets.base

import com.belcobtm.data.websockets.base.model.SocketResponse
import kotlinx.coroutines.flow.Flow

interface SocketClient {

    fun connect(url: String)

    fun sendMessage(message: String)

    fun observeMessages(): Flow<SocketResponse>

    fun close(code: Int, reason: String? = null)
}