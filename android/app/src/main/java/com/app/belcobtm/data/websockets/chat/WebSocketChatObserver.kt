package com.app.belcobtm.data.websockets.chat

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.app.belcobtm.data.websockets.base.model.SocketState
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.app.belcobtm.data.websockets.manager.WebSocketManager
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.mapSuspend
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext

class WebSocketChatObserver(
    private val socketManager: WebSocketManager,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val serializer: RequestSerializer<NewMessageItem>,
    private val deserializer: ResponseDeserializer<ChatMessageResponse>
) : ChatObserver {
    private companion object {
        const val ID_HEADER = "id"

        const val DESTINATION_HEADER = "destination"
        const val DESTINATION_VALUE = "/user/queue/chat"
        const val DESTINATION_SEND_VALUE = "/app/chat"
    }

    override suspend fun connect() {
        socketManager.observeSocketState()
            .filterIsInstance<SocketState.Connected>()
            .collect {
                val request = StompSocketRequest(
                    StompSocketRequest.SUBSCRIBE, mapOf(
                        ID_HEADER to sharedPreferencesHelper.userPhone,
                        DESTINATION_HEADER to DESTINATION_VALUE
                    )
                )
                socketManager.subscribe(DESTINATION_VALUE, request)
                    .filterNotNull()
                    .collect { response ->
                        response.mapSuspend {
                            tradeInMemoryCache.updateChat(deserializer.deserialize(it.body))
                        }
                    }
            }
    }

    override suspend fun disconnect() {
        socketManager.unsubscribe(DESTINATION_VALUE)
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
            socketManager.sendMessage(
                StompSocketRequest(
                    StompSocketRequest.MESSAGE,
                    mapOf(DESTINATION_HEADER to DESTINATION_SEND_VALUE),
                    serializer.serialize(messageItem)
                )
            )
        }
    }
}