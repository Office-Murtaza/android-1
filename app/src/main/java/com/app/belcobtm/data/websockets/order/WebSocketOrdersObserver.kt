package com.app.belcobtm.data.websockets.order

import android.util.Log
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.app.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.app.belcobtm.data.websockets.base.SocketClient
import com.app.belcobtm.data.websockets.base.model.SocketResponse
import com.app.belcobtm.data.websockets.base.model.SocketState
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.base.model.StompSocketResponse
import com.app.belcobtm.data.websockets.manager.SocketManager.Companion.DESTINATION_HEADER
import com.app.belcobtm.data.websockets.manager.SocketManager.Companion.ID_HEADER
import com.app.belcobtm.data.websockets.manager.WebSocketManager
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.domain.mapSuspend
import com.app.belcobtm.presentation.core.Endpoint
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class WebSocketOrdersObserver(
    private val socketManager: WebSocketManager,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val moshi: Moshi,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : OrdersObserver {

    private companion object {
        const val DESTINATION_VALUE = "/user/queue/order"
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
                                moshi.adapter(TradeOrderItemResponse::class.java)
                                    .fromJson(it.body)
                                    ?.let(tradeInMemoryCache::updateOrders)
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
}