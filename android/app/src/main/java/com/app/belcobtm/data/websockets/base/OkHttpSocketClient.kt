package com.app.belcobtm.data.websockets.base

import com.app.belcobtm.data.websockets.base.model.SocketResponse
import com.app.belcobtm.domain.Either
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import okhttp3.*

class OkHttpSocketClient(
    private val okHttpClient: OkHttpClient
) : WebSocketListener(), SocketClient {

    private var webSocket: WebSocket? = null
    private val messages = Channel<SocketResponse>(Channel.CONFLATED)

    override fun connect(url: String) {
        if (webSocket != null) {
            return
        }
        val request = Request.Builder().url(url).build()
        okHttpClient.newWebSocket(request, this)
    }

    override fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    override fun observeMessages(): Flow<SocketResponse> = messages.consumeAsFlow()

    override fun close(code: Int, reason: String?) {
        webSocket?.close(code, reason)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
        messages.sendBlocking(SocketResponse.Status(SocketResponse.Status.OPENED))
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        messages.sendBlocking(SocketResponse.Message(Either.Left(text)))
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        messages.sendBlocking(SocketResponse.Message(Either.Right(t)))
        this.webSocket = null
        messages.sendBlocking(SocketResponse.Status(SocketResponse.Status.FAILURE))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        this.webSocket = null
        messages.sendBlocking(SocketResponse.Status(SocketResponse.Status.DISCONNECTED))
    }
}