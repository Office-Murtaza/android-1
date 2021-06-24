package com.app.belcobtm.data.websockets.base

import android.util.Log
import com.app.belcobtm.data.websockets.base.model.SocketResponse
import com.app.belcobtm.domain.Failure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import okhttp3.*
import java.io.EOFException
import java.net.SocketException

class OkHttpSocketClient(
    private val okHttpClient: OkHttpClient
) : WebSocketListener(), SocketClient {

    private var webSocket: WebSocket? = null
    private val messages = MutableStateFlow<SocketResponse?>(null)

    companion object {
        const val NO_INTERNET_MESSAGE = "connection abort"
    }

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

    override fun observeMessages(): Flow<SocketResponse> = messages.filterNotNull()

    override fun close(code: Int, reason: String?) {
        webSocket?.close(code, reason)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
        messages.value = SocketResponse.Opened
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        messages.value = SocketResponse.Message(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        this.webSocket = null
        when {
            t is EOFException ->
                connect(webSocket.request().url().toString())
            t is SocketException && t.message.orEmpty().contains(NO_INTERNET_MESSAGE) ->
                messages.value = SocketResponse.Failure(Failure.NetworkConnection)
            else ->
                messages.value = SocketResponse.Failure(t)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        this.webSocket = null
        messages.value = SocketResponse.Disconnected
    }
}