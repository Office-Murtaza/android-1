package com.belcobtm.data.websockets.transactions

import android.util.Log
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.inmemory.transactions.TransactionsInMemoryCache
import com.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.belcobtm.data.websockets.base.model.SocketState
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.manager.SocketManager.Companion.DESTINATION_HEADER
import com.belcobtm.data.websockets.manager.SocketManager.Companion.ID_HEADER
import com.belcobtm.data.websockets.manager.WebSocketManager
import com.belcobtm.domain.map
import com.belcobtm.domain.mapSuspend
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class WebSocketTransactionsObserver(
    private val socketManager: WebSocketManager,
    private val moshi: Moshi,
    private val transactionsInMemoryCache: TransactionsInMemoryCache,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : TransactionsObserver {

    private companion object {
        const val DESTINATION_VALUE = "/user/queue/transaction"
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
                                moshi.adapter(TransactionDetailsResponse::class.java)
                                    .fromJson(it.body)
                                    ?.let(transactionsInMemoryCache::update)
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