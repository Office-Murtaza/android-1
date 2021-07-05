package com.belcobtm.data.websockets.chat

import android.util.Log
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.belcobtm.data.websockets.base.model.SocketState
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.belcobtm.data.websockets.manager.SocketManager.Companion.DESTINATION_HEADER
import com.belcobtm.data.websockets.manager.SocketManager.Companion.ID_HEADER
import com.belcobtm.data.websockets.manager.WebSocketManager
import com.belcobtm.data.websockets.serializer.RequestSerializer
import com.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.belcobtm.domain.mapSuspend
import com.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebSocketChatObserver(
    private val socketManager: WebSocketManager,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val serializer: RequestSerializer<NewMessageItem>,
    private val deserializer: ResponseDeserializer<ChatMessageResponse>
) : ChatObserver {

    private companion object {
        const val DESTINATION_VALUE = "/user/queue/chat"
        const val DESTINATION_SEND_VALUE = "/app/chat"
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun connect() {
        ioScope.launch {
            socketManager.observeSocketState()
                .filterIsInstance<SocketState.Connected>()
                .collectLatest {
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
    }

    override fun disconnect() {
        ioScope.launch {
            socketManager.unsubscribe(DESTINATION_VALUE)
        }
    }

    override fun sendMessage(messageItem: NewMessageItem) {
        ioScope.launch {
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