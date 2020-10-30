package com.app.belcobtm.data.sockets

import android.annotation.SuppressLint
import android.util.Log
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.domain.Failure
import okhttp3.*
import java.io.EOFException
import java.util.*

@SuppressLint("LogNotTimber")
class SocketClient(
    private val okHttpClient: OkHttpClient,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
) : WebSocketListener() {

    private val topics: MutableMap<String, SubscriptionsHandler?> = HashMap()
    private var webSocket: WebSocket? = null
    private var isConnected: Boolean = false

    var onFailure: (Failure) -> Unit = {}
    var onConnected: () -> Unit = {}

    fun connect() {
        Log.d(TAG,"socket connect called")
        val request = Request.Builder()
            .url(ApiFactory.SOCKET_URL)
            .build()
        okHttpClient.newWebSocket(request, this)
    }

    fun subscribe(topic: String): SubscriptionsHandler {
        Log.d(TAG,"Susbscribe called for $topic")
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
        Log.d(TAG,"Unsubscribe called for $topic")
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
        val message = SocketMessage(CONNECT_MESSAGE)
        message.put(ACCEPT_VERSION_PARAM, ACCEPT_VERSION_VALUE)
        message.put(HEARTBEAT_PARAM, HEARTBEAT_VALUE)
        message.put(
            AUTH_PARAM,
            sharedPreferencesHelper.accessToken
        )
        webSocket.send(SocketMessageSerializer.serialize(message))
    }

    private fun sendSubscribeMessage(webSocket: WebSocket, topic: String) {
        val message = SocketMessage(SUBSCRIBE_MESSAGE)
        message.put(ID_PARAM, sharedPreferencesHelper.userPhone)
        message.put(DESTINATION_PARAM, topic)
        webSocket.send(SocketMessageSerializer.serialize(message))
    }

    private fun sendUnSubscribeMessage(webSocket: WebSocket, topic: String) {
        val message = SocketMessage(UNSUBSCRIBE_MESSAGE)
        message.put(ID_PARAM, sharedPreferencesHelper.userPhone)
        message.put(DESTINATION_PARAM, topic)
        webSocket.send(SocketMessageSerializer.serialize(message))
    }

    fun disconnect() {
        webSocket?.close(1000, "socket closed")
    }

    fun isConnected(): Boolean {
        return isConnected
    }

    override fun onOpen(webSocket: WebSocket, response: Response?) {
        Log.d(TAG,"socket is opened")
        this.webSocket = webSocket
        isConnected = true
        onConnected()
        sendConnectMessage(webSocket)
        for (topic in topics.keys) {
            sendSubscribeMessage(webSocket, topic)
        }
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        Log.d(TAG,"socket message: $text")
        text?.run {
            val message = SocketMessageSerializer.deserialize(this)
            if (message.command == ERROR_MESSAGE) {
                disconnect()
            }
            val topic = message.getHeader(DESTINATION_PARAM)
            if (topics.containsKey(topic)) {
                topics[topic]!!.onMessage(message)
            }
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "closing called")
        webSocket.close(1000, null)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.d(TAG, "closed socket")
        isConnected = false
        this.webSocket = null
    }

    override fun onFailure(
        webSocket: WebSocket?,
        t: Throwable,
        response: Response?
    ) {
        Log.w(TAG, "failure", t)
        isConnected = false
        this.webSocket = null
        if (t !is EOFException) {
            onFailure(Failure.ServerError())
        }
    }

    companion object {
        private const val TAG = "SOCKETCLIENT"
        private const val CONNECT_MESSAGE = "CONNECT"
        private const val SUBSCRIBE_MESSAGE = "SUBSCRIBE"
        private const val UNSUBSCRIBE_MESSAGE = "UNSUBSCRIBE"
        private const val ERROR_MESSAGE = "ERROR"

        private const val ID_PARAM = "id"
        private const val DESTINATION_PARAM = "destination"
        private const val AUTH_PARAM = "Authorization"

        private const val ACCEPT_VERSION_PARAM = "accept-version"
        private const val ACCEPT_VERSION_VALUE = "1.1"

        private const val HEARTBEAT_PARAM = "heart-beat"
        private const val HEARTBEAT_VALUE = "1000,1000"
    }
}