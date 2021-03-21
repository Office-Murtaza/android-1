package com.app.belcobtm.data.websockets.trade

import android.util.Log
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.inmemory.TradeInMemoryCache
import com.app.belcobtm.data.websockets.base.SocketClient
import com.app.belcobtm.data.websockets.base.model.SocketResponse
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.data.websockets.wallet.model.WalletSocketRequest
import com.app.belcobtm.data.websockets.wallet.model.WalletSocketResponse
import com.app.belcobtm.presentation.core.Endpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebSocketTradesObserver(
    private val socketClient: SocketClient,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val serializer: RequestSerializer<WalletSocketRequest>,
    private val deserializer: ResponseDeserializer<WalletSocketResponse>,
) : TradesObserver {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private companion object {
        const val AUTH_HEADER = "Authorization"

        const val DESTINATION_HEADER = "destination"
        const val DESTINATION_VALUE = "/queue/order-chat"

        const val ACCEPT_VERSION_HEADER = "accept-version"
        const val ACCEPT_VERSION_VALUE = "1.1"

        const val HEARTBEAT_HEADER = "heart-beat"
        const val HEARTBEAT_VALUE = "1000,1000"

        const val HEADER_MESSAGE_KEY = "message"
        const val AUTH_ERROR_MESSAGE = "Access is denied"
    }

    init {
        ioScope.launch {
            socketClient.observeMessages()
                .collect {
                    when (it) {
                        is SocketResponse.Opened -> onOpened()
                        is SocketResponse.Failure ->
                            Log.e("TradesSocket", "Error", it.cause)
                        is SocketResponse.Message ->
                            processMessage(it.content)
                        is SocketResponse.Disconnected -> {
                            Log.d("TradesSocket", "Close connection")
                        }
                    }
                }
        }
    }

    override suspend fun connect() {
        withContext(ioScope.coroutineContext) {
            socketClient.connect(Endpoint.SOCKET_URL)
        }
    }

    override suspend fun disconnect() {
        withContext(ioScope.coroutineContext) {
            val request = WalletSocketRequest(
                WalletSocketRequest.UNSUBSCRIBE, mapOf(
                    DESTINATION_HEADER to DESTINATION_VALUE
                )
            )
            socketClient.sendMessage(serializer.serialize(request))
            socketClient.close(1000)
        }
    }

    private fun processMessage(content: String) {
        val response = deserializer.deserialize(content)
        when (response.status) {
            WalletSocketResponse.CONNECTED -> subscribe()
//            WalletSocketResponse.ERROR -> processErrorMessage(response)
            WalletSocketResponse.CONTENT -> {
                Log.d("TradesSocket", "Content $content")
            }
        }
    }

    private fun subscribe() {
        val request = WalletSocketRequest(WalletSocketRequest.SUBSCRIBE, mapOf(DESTINATION_HEADER to DESTINATION_VALUE))
        socketClient.sendMessage(serializer.serialize(request))
    }

    private fun onOpened() {
        val request = WalletSocketRequest(
            WalletSocketRequest.CONNECT, mapOf(
                ACCEPT_VERSION_HEADER to ACCEPT_VERSION_VALUE,
                AUTH_HEADER to sharedPreferencesHelper.accessToken,
                HEARTBEAT_HEADER to HEARTBEAT_VALUE
            )
        )
        socketClient.sendMessage(serializer.serialize(request))
    }
}