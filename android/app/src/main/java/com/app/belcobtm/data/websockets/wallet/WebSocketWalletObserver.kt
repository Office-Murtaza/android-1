package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.data.disk.database.account.AccountDao
import com.app.belcobtm.data.disk.database.wallet.WalletDao
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.response.BalanceResponse
import com.app.belcobtm.data.websockets.base.model.SocketState
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.base.model.StompSocketResponse
import com.app.belcobtm.data.websockets.manager.SocketManager
import com.app.belcobtm.data.websockets.manager.SocketManager.Companion.COINS_HEADER
import com.app.belcobtm.data.websockets.manager.SocketManager.Companion.DESTINATION_HEADER
import com.app.belcobtm.data.websockets.manager.SocketManager.Companion.ID_HEADER
import com.app.belcobtm.data.websockets.manager.WebSocketManager
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.domain.Failure
import com.squareup.moshi.Moshi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*

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
        const val AUTH_ERROR_MESSAGE = "Access is denied"
    }

    private val connectionFailure = ConflatedBroadcastChannel<Failure?>()

    override suspend fun connect() {
        socketManager.observeSocketState()
            .filterIsInstance<SocketState.Connected>()
            .collect {
                connectionFailure.send(null)
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

    private suspend fun processMessage(response: StompSocketResponse) {
        moshi.adapter(BalanceResponse::class.java)
            .fromJson(response.body)
            ?.let { balanceResponse ->
                walletDao.updateBalance(balanceResponse)
            }
    }

    override fun observeConnectionFailure(): Flow<Failure?> =
        connectionFailure.asFlow()

    override suspend fun disconnect() {
        socketManager.unsubscribe(DESTINATION_VALUE)
    }

    private suspend fun processError(failure: Failure) {
        connectionFailure.send(failure)
    }
}