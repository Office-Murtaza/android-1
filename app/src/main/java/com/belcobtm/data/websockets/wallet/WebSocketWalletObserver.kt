package com.belcobtm.data.websockets.wallet

import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.wallet.response.BalanceResponse
import com.belcobtm.data.websockets.base.model.SocketState
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.base.model.StompSocketResponse
import com.belcobtm.data.websockets.manager.SocketManager.Companion.DESTINATION_HEADER
import com.belcobtm.data.websockets.manager.SocketManager.Companion.ID_HEADER
import com.belcobtm.data.websockets.manager.WebSocketManager
import com.belcobtm.domain.Failure
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@kotlinx.coroutines.ExperimentalCoroutinesApi
class WebSocketWalletObserver(
    private val socketManager: WebSocketManager,
    private val walletDao: WalletDao,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val moshi: Moshi
) : WalletConnectionHandler {

    private companion object {

        const val DESTINATION_VALUE = "/user/queue/balance"
    }

    private val connectionFailure = MutableStateFlow<Failure?>(null)
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var subscribeJob: Job? = null

    override fun connect() {
        subscribeJob = ioScope.launch {
            socketManager.observeSocketState()
                .filterIsInstance<SocketState.Connected>()
                .flatMapLatest {
                    connectionFailure.value = null
                    val request = StompSocketRequest(
                        StompSocketRequest.SUBSCRIBE, mapOf(
                            ID_HEADER to sharedPreferencesHelper.userPhone,
                            DESTINATION_HEADER to DESTINATION_VALUE,
                        )
                    )
                    socketManager.subscribe(DESTINATION_VALUE, request)
                }.filterNotNull()
                .collect { response ->
                    response.eitherSuspend(::processError, ::processMessage)
                }
        }
    }

    private suspend fun processMessage(response: StompSocketResponse) {
        moshi.adapter(BalanceResponse::class.java)
            .fromJson(response.body)
            ?.let { balanceResponse ->
                walletDao.updateBalance(balanceResponse)
            }
    }

    override fun observeConnectionFailure(): Flow<Failure?> =
        connectionFailure

    private fun processError(failure: Failure) {
        connectionFailure.value = failure
    }

}
