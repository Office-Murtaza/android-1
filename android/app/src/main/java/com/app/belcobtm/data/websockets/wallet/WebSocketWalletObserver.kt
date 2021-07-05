package com.belcobtm.data.websockets.wallet

import android.util.Log
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.wallet.response.BalanceResponse
import com.belcobtm.data.websockets.base.model.SocketState
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.base.model.StompSocketResponse
import com.belcobtm.data.websockets.manager.SocketManager.Companion.COINS_HEADER
import com.belcobtm.data.websockets.manager.SocketManager.Companion.DESTINATION_HEADER
import com.belcobtm.data.websockets.manager.SocketManager.Companion.ID_HEADER
import com.belcobtm.data.websockets.manager.WebSocketManager
import com.belcobtm.domain.Failure
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@kotlinx.coroutines.ExperimentalCoroutinesApi
class WebSocketWalletObserver(
    private val socketManager: WebSocketManager,
    private val accountDao: AccountDao,
    private val walletDao: WalletDao,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val moshi: Moshi
) : WalletConnectionHandler {

    private companion object {
        const val DESTINATION_VALUE = "/user/queue/balance"
    }

    private val connectionFailure = MutableStateFlow<Failure?>(null)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun connect() {
        ioScope.launch {
            socketManager.observeSocketState()
                .filterIsInstance<SocketState.Connected>()
                .collectLatest {
                    connectionFailure.value = null
                    val coinList = accountDao.getItemList().orEmpty().joinToString { it.type.name }
                    val request = StompSocketRequest(
                        StompSocketRequest.SUBSCRIBE, mapOf(
                            ID_HEADER to sharedPreferencesHelper.userPhone,
                            DESTINATION_HEADER to DESTINATION_VALUE,
                            COINS_HEADER to coinList
                        )
                    )
                    socketManager.subscribe(DESTINATION_VALUE, request)
                        .filterNotNull()
                        .collect { response ->
                            response.eitherSuspend(::processError, ::processMessage)
                        }
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

    override fun disconnect() {
        ioScope.launch {
            socketManager.unsubscribe(DESTINATION_VALUE)
        }
    }

    private fun processError(failure: Failure) {
        connectionFailure.value = failure
    }
}