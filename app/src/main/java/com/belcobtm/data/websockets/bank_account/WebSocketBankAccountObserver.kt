package com.belcobtm.data.websockets.bank_account

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.inmemory.bank_accounts.BankAccountsInMemoryCache
import com.belcobtm.data.rest.bank_account.response.BankAccountResponse
import com.belcobtm.data.websockets.base.model.SocketState
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.manager.SocketManager
import com.belcobtm.data.websockets.manager.WebSocketManager
import com.belcobtm.domain.mapSuspend
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class WebSocketBankAccountObserver(
    private val socketManager: WebSocketManager,
    private val moshi: Moshi,
    private val bankAccountsInMemoryCache: BankAccountsInMemoryCache,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : BankAccountObserver {
    private companion object {
        const val DESTINATION_VALUE = "/user/queue/bank-account"
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var subscribeJob: Job? = null

    override fun connect() {
        subscribeJob = ioScope.launch {
            socketManager.observeSocketState()
                .filterIsInstance<SocketState.Connected>()
                .collectLatest {
                    val request = StompSocketRequest(
                        StompSocketRequest.SUBSCRIBE, mapOf(
                            SocketManager.ID_HEADER to sharedPreferencesHelper.userPhone,
                            SocketManager.DESTINATION_HEADER to DESTINATION_VALUE
                        )
                    )
                    socketManager.subscribe(DESTINATION_VALUE, request)
                        .filterNotNull()
                        .collect { response ->
                            response.mapSuspend {
                                moshi.adapter(BankAccountResponse::class.java)
                                    .fromJson(it.body)
                                    ?.let(bankAccountsInMemoryCache::update)
                            }
                        }
                }
        }
    }

    override fun disconnect() {
        if (subscribeJob == null) {
            return
        }
        ioScope.launch {
            subscribeJob?.cancel()
            subscribeJob = null
            socketManager.unsubscribe(DESTINATION_VALUE)
        }
    }
}