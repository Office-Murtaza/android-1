package com.app.belcobtm.data.sockets

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.domain.Failure
import okhttp3.*
import timber.log.Timber
import java.util.*

class SocketClient(
    private val okHttpClient: OkHttpClient,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : WebSocketListener() {

    private val topics: MutableMap<String, SubscriptionsHandler?> = HashMap()
    private var webSocket: WebSocket? = null
    private var isConnected: Boolean = false

    var onFailure: (Failure) -> Unit = {}
    var onConnected: () -> Unit = {}

    fun connect() {
        Timber.d("socket connect called")
        val request = Request.Builder()
            .url(ApiFactory.SOCKET_URL)
            .build()
        okHttpClient.newWebSocket(request, this)
    }

    fun subscribe(topic: String): SubscriptionsHandler {
        val handler = SubscriptionsHandler(topic)
        topics[topic] = handler
        if (webSocket != null) {
            sendSubscribeMessage(webSocket!!, topic)
        }
        return handler
    }

    fun unSubscribe(topic: String?) {
        topics.remove(topic)
    }

    fun getTopicHandler(topic: String?): SubscriptionsHandler? {
        return if (topics.containsKey(topic)) {
            topics[topic]
        } else null
    }

    private fun sendConnectMessage(webSocket: WebSocket) {
        val message = SocketMessage("CONNECT")
        message.put("accept-version", "1.1")
        message.put("heart-beat", "10000,10000")
        message.put(
            "Authorization",
            sharedPreferencesHelper.accessToken
        )
        webSocket.send(SocketMessageSerializer.serialize(message))
    }

    private fun sendSubscribeMessage(webSocket: WebSocket, topic: String) {
        val message = SocketMessage("SUBSCRIBE")
        message.put("id", sharedPreferencesHelper.userPhone)
        message.put("destination", topic)
        webSocket.send(SocketMessageSerializer.serialize(message))
    }

    fun disconnect() {
        webSocket?.close(1000, "socket closed")
    }

    fun isConnected(): Boolean {
        return isConnected
    }

    override fun onOpen(webSocket: WebSocket, response: Response?) {
        Timber.d("socket is opened")
        this.webSocket = webSocket
        isConnected = true
        onConnected()
        sendConnectMessage(webSocket)
        for (topic in topics.keys) {
            sendSubscribeMessage(webSocket, topic)
        }
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        Timber.d("socket message: $text")
        text?.run {
            val message = SocketMessageSerializer.deserialize(this)
            val topic = message.getHeader("destination")
            if (topics.containsKey(topic)) {
                topics[topic]!!.onMessage(message)
            }
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        isConnected = false
        this.webSocket = null
    }

    override fun onFailure(
        webSocket: WebSocket?,
        t: Throwable,
        response: Response?
    ) {
        Timber.w(t, "failure")
        onFailure(Failure.ServerError())
    }
}