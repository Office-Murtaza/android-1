package com.app.belcobtm.data.websockets.trade

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.app.belcobtm.data.rest.trade.response.TradeItemResponse
import com.app.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.app.belcobtm.data.websockets.base.SocketClient
import com.app.belcobtm.data.websockets.base.model.SocketResponse
import com.app.belcobtm.data.websockets.base.model.SocketState
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.base.model.StompSocketResponse
import com.app.belcobtm.data.websockets.manager.SocketManager
import com.app.belcobtm.data.websockets.manager.SocketManager.Companion.DESTINATION_HEADER
import com.app.belcobtm.data.websockets.manager.SocketManager.Companion.ID_HEADER
import com.app.belcobtm.data.websockets.manager.WebSocketManager
import com.app.belcobtm.data.websockets.order.WebSocketOrdersObserver
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.domain.mapSuspend
import com.app.belcobtm.presentation.core.Endpoint
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull

class WebSocketTradesObserver(
    private val socketManager: WebSocketManager,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val moshi: Moshi,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : TradesObserver {

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var reconnectCounter = 0

    private companion object {
        const val DESTINATION_VALUE = "/topic/trade"
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
                            moshi.adapter(TradeItemResponse::class.java)
                                .fromJson(it.body)
                                ?.let(tradeInMemoryCache::updateTrades)
                        }
                    }
            }
    }

    override suspend fun disconnect() {
        socketManager.unsubscribe(DESTINATION_VALUE)
    }
}