package com.app.belcobtm.data.websockets.chat

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebSocketChatObserver(
    private val socketClient: SocketClient,
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

        const val DESTINATION_HEADER = "destination"
        const val DESTINATION_VALUE = "/queue/order-chat"

        const val ACCEPT_VERSION_HEADER = "accept-version"
        const val ACCEPT_VERSION_VALUE = "1.1"

        const val HEARTBEAT_HEADER = "heart-beat"
        const val HEARTBEAT_VALUE = "1000,1000"

        const val HEADER_MESSAGE_KEY = "message"
        const val AUTH_ERROR_MESSAGE = "Access is denied"
    }

    private val chatData = MutableStateFlow<List<ChatMessageResponse>>(emptyList())

    init {
        ioScope.launch {
            socketClient.observeMessages()
                .collect {
                    when (it) {
                        is SocketResponse.Opened -> onOpened()
                        is SocketResponse.Failure ->
                            connect()
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

    override fun observeChatMessages(): Flow<List<ChatMessageResponse>> = chatData

    override suspend fun sendMessage(messageItem: NewMessageItem) {
        withContext(Dispatchers.IO) {
            socketClient.sendMessage(serializer.serialize(messageItem))
        }
    }

    private fun processMessage(content: String) {
        val response = stompDeserializer.deserialize(content)
        when (response.status) {
            StompSocketResponse.CONNECTED -> subscribe()
            StompSocketResponse.CONTENT -> {
                chatData.value += deserializer.deserialize(content)
            }
        }
    }

    private fun subscribe() {
        val request = StompSocketRequest(
            StompSocketRequest.SUBSCRIBE, mapOf(
                ID_HEADER to sharedPreferencesHelper.userPhone,
                DESTINATION_HEADER to DESTINATION_VALUE,
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