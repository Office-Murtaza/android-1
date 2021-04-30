package com.app.belcobtm.data.websockets.chat

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.inmemory.TradeInMemoryCache
import com.app.belcobtm.data.websockets.base.SocketClient
import com.app.belcobtm.data.websockets.base.model.SocketResponse
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.base.model.StompSocketResponse
import com.app.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.presentation.core.Endpoint
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebSocketChatObserver(
    private val socketClient: SocketClient,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val stompSerializer: RequestSerializer<StompSocketRequest>,
    private val stompDeserializer: ResponseDeserializer<StompSocketResponse>,
    private val serializer: RequestSerializer<NewMessageItem>,
    private val deserializer: ResponseDeserializer<ChatMessageResponse>
) : ChatObserver {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private companion object {
        const val ID_HEADER = "id"
        const val AUTH_HEADER = "Authorization"
        const val COINS_HEADER = "coins"

        const val DESTINATION_HEADER = "destination"
        const val DESTINATION_VALUE = "/user/queue/order-chat"
        const val DESTINATION_SEND_VALUE = "/app/order-chat"

        const val ACCEPT_VERSION_HEADER = "accept-version"
        const val ACCEPT_VERSION_VALUE = "1.1"

        const val HEARTBEAT_HEADER = "heart-beat"
        const val HEARTBEAT_VALUE = "1000,1000"
    }

    init {
        ioScope.launch {
            socketClient.observeMessages()
                .collect {
                    when (it) {
                        is SocketResponse.Opened -> onOpened()
                        is SocketResponse.Message ->
                            processMessage(it.content)
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
            val request = StompSocketRequest(
                StompSocketRequest.UNSUBSCRIBE, mapOf(
                    DESTINATION_HEADER to DESTINATION_VALUE
                )
            )
            socketClient.sendMessage(stompSerializer.serialize(request))
            socketClient.close(1000)
        }
    }

    override suspend fun sendMessage(messageItem: NewMessageItem) {
        withContext(Dispatchers.IO) {
            tradeInMemoryCache.updateChat(
                ChatMessageResponse(
                    messageItem.orderId,
                    messageItem.fromId,
                    messageItem.toId,
                    messageItem.content,
                    messageItem.attachmentName,
                    System.currentTimeMillis()
                )
            )
            socketClient.sendMessage(
                stompSerializer.serialize(
                    StompSocketRequest(
                        StompSocketRequest.MESSAGE,
                        mapOf(DESTINATION_HEADER to DESTINATION_SEND_VALUE),
                        serializer.serialize(messageItem)
                    )
                )
            )
        }
    }

    private suspend fun processMessage(content: String) {
        val response = stompDeserializer.deserialize(content)
        when (response.status) {
            StompSocketResponse.CONNECTED -> subscribe()
            StompSocketResponse.CONTENT -> {
                val response = stompDeserializer.deserialize(content)
                tradeInMemoryCache.updateChat(deserializer.deserialize(response.body))
            }
        }
    }

    private fun subscribe() {
        val request = StompSocketRequest(
            StompSocketRequest.SUBSCRIBE, mapOf(
                ID_HEADER to sharedPreferencesHelper.userPhone,
                DESTINATION_HEADER to DESTINATION_VALUE
            )
        )
        socketClient.sendMessage(stompSerializer.serialize(request))
    }

    private fun onOpened() {
        val request = StompSocketRequest(
            StompSocketRequest.CONNECT, mapOf(
                ACCEPT_VERSION_HEADER to ACCEPT_VERSION_VALUE,
                AUTH_HEADER to sharedPreferencesHelper.accessToken,
                HEARTBEAT_HEADER to HEARTBEAT_VALUE
            )
        )
        socketClient.sendMessage(stompSerializer.serialize(request))
    }
}