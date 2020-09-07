package com.app.belcobtm.data.sockets

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor.Companion.KEY_IS_USER_UNAUTHORIZED
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor.Companion.TAG_USER_AUTHORIZATION
import com.app.belcobtm.domain.Failure
import okhttp3.*
import timber.log.Timber
import java.util.*

class SocketClient(
    private val okHttpClient: OkHttpClient,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val broadcastManager: LocalBroadcastManager
) : WebSocketListener() {

    private val topics: MutableMap<String, SubscriptionsHandler?> = HashMap()
    private var webSocket: WebSocket? = null
    private var isConnected: Boolean = false

    var onFailure: (Failure) -> Unit = {}
    var onConnected: () -> Unit = {}

    fun connect() {
        Log.d("SOCKETCLIENT","socket connect called")
        val request = Request.Builder()
            .url(ApiFactory.SOCKET_URL)
            .build()
        okHttpClient.newWebSocket(request, this)
    }

    fun subscribe(topic: String): SubscriptionsHandler {
        Log.d("SOCKETCLIENT","Susbscribe called for $topic")
        topics[topic]?.let {
            return it
        }
        val handler = SubscriptionsHandler(topic)
        topics[topic] = handler
        if (webSocket != null && isConnected) {
            sendSubscribeMessage(webSocket!!, topic)
        } else {
            connect()
        }
        return handler
    }

    fun unSubscribe(topic: String) {
        Log.d("SOCKETCLIENT","Unsubscribe called for $topic")
        topics.remove(topic)
        if (webSocket != null && isConnected) {
            sendUnSubscribeMessage(webSocket!!, topic)
        }
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

    private fun sendUnSubscribeMessage(webSocket: WebSocket, topic: String) {
        val message = SocketMessage("UNSUBSCRIBE")
        message.put("id", sharedPreferencesHelper.userPhone)
        message.put("destination", topic)
        webSocket.send(SocketMessageSerializer.serialize(message))
    }

    private fun goToLogin() {
        val intent = Intent(TAG_USER_AUTHORIZATION)
        intent.putExtra(KEY_IS_USER_UNAUTHORIZED, true)
        broadcastManager.sendBroadcast(intent)
    }

    fun disconnect() {
        webSocket?.close(1000, "socket closed")
    }

    fun isConnected(): Boolean {
        return isConnected
    }

    override fun onOpen(webSocket: WebSocket, response: Response?) {
        Log.d("SOCKETCLIENT","socket is opened")
        this.webSocket = webSocket
        isConnected = true
        onConnected()
        sendConnectMessage(webSocket)
        for (topic in topics.keys) {
            sendSubscribeMessage(webSocket, topic)
        }
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        Log.d("SOCKETCLIENT","socket message: $text")
        text?.run {
            val message = SocketMessageSerializer.deserialize(this)
            val topic = message.getHeader("destination")
            if (topics.containsKey(topic)) {
                topics[topic]!!.onMessage(message)
            }
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("SOCKETCLIENT", "closing called")
        webSocket.close(1000, null)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.d("SOCKETCLIENT", "closed socket")
        isConnected = false
        this.webSocket = null
    }

    override fun onFailure(
        webSocket: WebSocket?,
        t: Throwable,
        response: Response?
    ) {
        Log.w("SOCKETCLIENT", "failure", t)
        isConnected = false
        this.webSocket = null
        onFailure(Failure.ServerError())
    }
}