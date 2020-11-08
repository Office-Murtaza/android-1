package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.database.AccountEntity
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.response.BalanceResponse
import com.app.belcobtm.data.rest.wallet.response.mapToDataItem
import com.app.belcobtm.data.websockets.base.SocketClient
import com.app.belcobtm.data.websockets.base.model.SocketResponse
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.data.websockets.wallet.model.WalletBalance
import com.app.belcobtm.data.websockets.wallet.model.WalletSocketRequest
import com.app.belcobtm.data.websockets.wallet.model.WalletSocketResponse
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.Endpoint
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import com.app.belcobtm.data.websockets.base.model.SocketResponse.Status.Companion as SocketStatus

@kotlinx.coroutines.ExperimentalCoroutinesApi
class WebSocketWalletObserver(
    private val socketClient: SocketClient,
    private val accountDao: AccountDao,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val serializer: RequestSerializer<WalletSocketRequest>,
    private val deserializer: ResponseDeserializer<WalletSocketResponse>,
    private val moshi: Moshi
) : WalletObserver {

    private companion object {
        const val ID_HEADER = "id"
        const val AUTH_HEADER = "Authorization"
        const val COINS_HEADER = "coins"

        const val DESTINATION_HEADER = "destination"
        const val DESTINATION_VALUE = "/user/queue/balance"

        const val ACCEPT_VERSION_HEADER = "accept-version"
        const val ACCEPT_VERSION_VALUE = "1.1"

        const val HEARTBEAT_HEADER = "heart-beat"
        const val HEARTBEAT_VALUE = "1000,1000"
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val balanceInfo = ConflatedBroadcastChannel<WalletBalance>()

    init {
        ioScope.launch {
            socketClient.observeMessages()
                .collect {
                    when (it) {
                        is SocketResponse.Status -> processStatus(it.code)
                        is SocketResponse.Message -> it.content.either({
                            runBlocking {
                                processMessage(it)
                            }
                        }) {
                            runBlocking {
                                processError()
                            }
                        }
                    }
                }
        }
    }

    override fun observe(): Flow<WalletBalance> {
        val subscribeChannel = balanceInfo.openSubscription()
        return flow<WalletBalance> {
            emitAll(subscribeChannel)
        }.onCompletion {
            subscribeChannel.cancel()
        }
    }

    override suspend fun connect() {
        withContext(ioScope.coroutineContext) {
            balanceInfo.send(WalletBalance.NoInfo)
            socketClient.connect(Endpoint.SOCKET_URL)
        }
    }

    override suspend fun disconnect() {
        withContext(ioScope.coroutineContext) {
            val request = WalletSocketRequest(
                WalletSocketRequest.UNSUBSCRIBE, mapOf(
                    ID_HEADER to sharedPreferencesHelper.userPhone,
                    DESTINATION_HEADER to DESTINATION_VALUE
                )
            )
            socketClient.sendMessage(serializer.serialize(request))
            socketClient.close(1000)
            balanceInfo.send(WalletBalance.NoInfo)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun processMessage(content: String) {
        val response = deserializer.deserialize(content)
        when (response.status) {
            WalletSocketResponse.CONNECTED -> subscribe()
            WalletSocketResponse.ERROR -> processError()
            WalletSocketResponse.CONTENT -> {
                moshi.adapter(BalanceResponse::class.java)
                    .fromJson(response.body)
                    ?.let { balance ->
                        balanceInfo.send(WalletBalance.Balance(balance.mapToDataItem()))
                    }
            }
        }
    }

    private fun subscribe() {
        val coinList = runBlocking {
            accountDao.getItemList().orEmpty()
                .filter(AccountEntity::isEnabled)
                .map { it.type.name }
        }.joinToString()
        val request = WalletSocketRequest(
            WalletSocketRequest.SUBSCRIBE, mapOf(
                ID_HEADER to sharedPreferencesHelper.userPhone,
                DESTINATION_HEADER to DESTINATION_VALUE,
                COINS_HEADER to coinList
            )
        )
        socketClient.sendMessage(serializer.serialize(request))
    }

    private suspend fun processError() {
        balanceInfo.send(WalletBalance.Error(Failure.ServerError()))
    }

    private fun processStatus(@SocketStatus.Code code: Int) {
        when (code) {
            SocketStatus.OPENED -> {
                val request = WalletSocketRequest(
                    WalletSocketRequest.CONNECT, mapOf(
                        ACCEPT_VERSION_HEADER to ACCEPT_VERSION_VALUE,
                        AUTH_HEADER to sharedPreferencesHelper.accessToken,
                        HEARTBEAT_HEADER to HEARTBEAT_VALUE
                    )
                )
                socketClient.sendMessage(serializer.serialize(request))
            }
        }
    }
}