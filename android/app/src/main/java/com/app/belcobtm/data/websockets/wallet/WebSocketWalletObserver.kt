package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.authorization.request.RefreshTokenRequest
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
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collect
import java.net.HttpURLConnection

@kotlinx.coroutines.ExperimentalCoroutinesApi
class WebSocketWalletObserver(
    private val socketClient: SocketClient,
    private val accountDao: AccountDao,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val serializer: RequestSerializer<WalletSocketRequest>,
    private val deserializer: ResponseDeserializer<WalletSocketResponse>,
    private val moshi: Moshi,
    private val preferencesHelper: SharedPreferencesHelper,
    private val authApi: AuthApi
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

        const val HEADER_MESSAGE_KEY = "message"
        const val AUTH_ERROR_MESSAGE = "Access is denied"
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val balanceInfo = ConflatedBroadcastChannel<WalletBalance>()

    init {
        ioScope.launch {
            socketClient.observeMessages()
                .collect {
                    when (it) {
                        is SocketResponse.Opened -> onOpened()
                        is SocketResponse.Failure ->
                            runBlocking {
                                processError(it.cause)
                            }
                        is SocketResponse.Message -> it.content.either({
                            runBlocking {
                                processMessage(it)
                            }
                        }) {
                            runBlocking {
                                processError(it)
                            }
                        }
                    }
                }
        }
    }

    override fun observe(): ReceiveChannel<WalletBalance> =
        balanceInfo.openSubscription()

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
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun processMessage(content: String) {
        val response = deserializer.deserialize(content)
        when (response.status) {
            WalletSocketResponse.CONNECTED -> subscribe()
            WalletSocketResponse.ERROR -> processErrorMessage(response)
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

    private suspend fun processError(throwable: Throwable) {
        val error = when (throwable) {
            is Failure -> throwable
            else -> Failure.ServerError()
        }
        balanceInfo.send(WalletBalance.Error(error))
    }

    private suspend fun processErrorMessage(socketResponse: WalletSocketResponse) {
        val isTokenExpired = socketResponse.headers[HEADER_MESSAGE_KEY]
            .orEmpty()
            .contains(AUTH_ERROR_MESSAGE)
        if (isTokenExpired) {
            val request = RefreshTokenRequest(preferencesHelper.refreshToken)
            val response = authApi.refereshToken(request).execute()
            val responseBody = response.body()
            if (response.code() == HttpURLConnection.HTTP_OK && responseBody != null) {
                preferencesHelper.processAuthResponse(responseBody)
                disconnect()
                connect()
            } else {
                balanceInfo.send(WalletBalance.Error(Failure.ServerError()))
            }
        } else {
            balanceInfo.send(WalletBalance.Error(Failure.ServerError()))
        }
    }

    private fun onOpened() {
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