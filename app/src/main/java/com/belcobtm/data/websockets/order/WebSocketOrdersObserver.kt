package com.belcobtm.data.websockets.order

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.belcobtm.data.websockets.base.model.SocketState
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.manager.SocketManager.Companion.DESTINATION_HEADER
import com.belcobtm.data.websockets.manager.SocketManager.Companion.ID_HEADER
import com.belcobtm.data.websockets.manager.WebSocketManager
import com.belcobtm.domain.mapSuspend
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
    private var subscribeJob: Job? = null

    override fun connect() {
        subscribeJob = ioScope.launch {
            socketManager.observeSocketState()
                .filterIsInstance<SocketState.Connected>()
                .flatMapLatest {
                    val request = StompSocketRequest(
                        StompSocketRequest.SUBSCRIBE, mapOf(
                            ID_HEADER to sharedPreferencesHelper.userPhone,
                            DESTINATION_HEADER to DESTINATION_VALUE
                        )
                    )
                    socketManager.subscribe(DESTINATION_VALUE, request)
                }.filterNotNull()
                .collectLatest { response ->
                    response.mapSuspend { response ->
                        moshi.adapter(TradeOrderItemResponse::class.java)
                            .fromJson(response.body)
                            ?.let {
                                tradeInMemoryCache.updateOrders(it)
                            }
                    }
                }
        }
    }

    override fun disconnect() {
        if(subscribeJob == null) {
            return
        }
        ioScope.launch {
            subscribeJob?.cancel()
            subscribeJob = null
            socketManager.unsubscribe(DESTINATION_VALUE)
        }
    }
}