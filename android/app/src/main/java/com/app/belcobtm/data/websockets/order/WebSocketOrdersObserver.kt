package com.app.belcobtm.data.websockets.order

import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.inmemory.TradeInMemoryCache
import com.app.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.app.belcobtm.data.websockets.base.SocketClient
import com.app.belcobtm.data.websockets.base.model.SocketResponse
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.base.model.StompSocketResponse
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.presentation.core.Endpoint
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class WebSocketOrdersObserver(
    private val socketClient: SocketClient,
    private val tradeInMemoryCache: TradeInMemoryCache,
    private val accountDao: AccountDao,
    private val moshi: Moshi,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val serializer: RequestSerializer<StompSocketRequest>,
    private val deserializer: ResponseDeserializer<StompSocketResponse>,
) : OrdersObserver {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private companion object {
        const val ID_HEADER = "id"
        const val AUTH_HEADER = "Authorization"
        const val COINS_HEADER = "coins"

        const val DESTINATION_HEADER = "destination"
        const val DESTINATION_VALUE = "/queue/order"

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
            socketClient.sendMessage(serializer.serialize(request))
            socketClient.close(1000)
        }
    }

    private fun processMessage(content: String) {
        val response = deserializer.deserialize(content)
        when (response.status) {
            StompSocketResponse.CONNECTED -> subscribe()
//            StompSocketResponse.ERROR -> processErrorMessage(response)
            StompSocketResponse.CONTENT -> {
                moshi.adapter(TradeOrderItemResponse::class.java)
                    .fromJson(response.body)
                    ?.let(tradeInMemoryCache::updateOrders)
            }
        }
    }

    private fun subscribe() {
        val coinList = runBlocking {
            accountDao.getItemList().orEmpty()
                .map { it.type.name }
        }.joinToString()
        val request = StompSocketRequest(
            StompSocketRequest.SUBSCRIBE, mapOf(
                ID_HEADER to sharedPreferencesHelper.userPhone,
                DESTINATION_HEADER to DESTINATION_VALUE,
                COINS_HEADER to coinList
            )
        )
        socketClient.sendMessage(serializer.serialize(request))
    }

    private fun onOpened() {
        val request = StompSocketRequest(
            StompSocketRequest.CONNECT, mapOf(
                ACCEPT_VERSION_HEADER to ACCEPT_VERSION_VALUE,
                AUTH_HEADER to sharedPreferencesHelper.accessToken,
                HEARTBEAT_HEADER to HEARTBEAT_VALUE
            )
        )
        socketClient.sendMessage(serializer.serialize(request))
    }
}