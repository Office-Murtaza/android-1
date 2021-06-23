package com.app.belcobtm.data.websockets.transactions

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.inmemory.transactions.TransactionsInMemoryCache
import com.app.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.app.belcobtm.data.websockets.base.model.SocketState
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.manager.SocketManager.Companion.DESTINATION_HEADER
import com.app.belcobtm.data.websockets.manager.SocketManager.Companion.ID_HEADER
import com.app.belcobtm.data.websockets.manager.WebSocketManager
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.mapSuspend
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull

class WebSocketTransactionsObserver(
    private val socketManager: WebSocketManager,
    private val moshi: Moshi,
    private val transactionsInMemoryCache: TransactionsInMemoryCache,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : TransactionsObserver {

    private companion object {
        const val DESTINATION_VALUE = "/user/queue/transaction"
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
                            moshi.adapter(TransactionDetailsResponse::class.java)
                                .fromJson(it.body)
                                ?.let(transactionsInMemoryCache::update)
                        }
                    }
            }
    }

    override suspend fun disconnect() {
        socketManager.unsubscribe(DESTINATION_VALUE)
    }
}