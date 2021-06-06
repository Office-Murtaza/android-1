package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.data.core.UnlinkHandler
import com.app.belcobtm.data.disk.database.account.AccountDao
import com.app.belcobtm.data.disk.database.wallet.CoinDetailsEntity
import com.app.belcobtm.data.disk.database.wallet.CoinEntity
import com.app.belcobtm.data.disk.database.wallet.WalletDao
import com.app.belcobtm.data.disk.database.wallet.WalletEntity
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.app.belcobtm.data.rest.wallet.response.BalanceResponse
import com.app.belcobtm.data.websockets.base.SocketClient
import com.app.belcobtm.data.websockets.base.model.SocketResponse
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.base.model.StompSocketResponse
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.Endpoint
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import java.net.HttpURLConnection

@kotlinx.coroutines.ExperimentalCoroutinesApi
class WebSocketWalletObserver(
    private val socketClient: SocketClient,
    private val accountDao: AccountDao,
    private val walletDao: WalletDao,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val serializer: RequestSerializer<StompSocketRequest>,
    private val deserializer: ResponseDeserializer<StompSocketResponse>,
    private val moshi: Moshi,
    private val preferencesHelper: SharedPreferencesHelper,
    private val authApi: AuthApi,
    private val unlinkHandler: UnlinkHandler
) : WalletConnectionHandler {

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
    private val connectionFailure = ConflatedBroadcastChannel<Failure?>()

    init {
        ioScope.launch {
            socketClient.observeMessages()
                .collect {
                    when (it) {
                        is SocketResponse.Opened -> onOpened()
                        is SocketResponse.Failure ->
                            processError(it.cause)
                        is SocketResponse.Message ->
                            processMessage(it.content)
                    }
                }
        }
    }

    override suspend fun connect() {
        withContext(ioScope.coroutineContext) {
            connectionFailure.send(null)
            socketClient.connect(Endpoint.SOCKET_URL)
        }
    }

    override fun observeConnectionFailure(): Flow<Failure?> =
        connectionFailure.asFlow()

    override suspend fun disconnect() {
        withContext(ioScope.coroutineContext) {
            val request = StompSocketRequest(
                StompSocketRequest.UNSUBSCRIBE, mapOf(
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
            StompSocketResponse.CONNECTED -> subscribe()
            StompSocketResponse.ERROR -> processErrorMessage(response)
            StompSocketResponse.CONTENT -> {
                moshi.adapter(BalanceResponse::class.java)
                    .fromJson(response.body)
                    ?.let { balanceResponse ->
                        val wallet = WalletEntity(balanceResponse.totalBalance)
                        val coins = ArrayList<CoinEntity>()
                        val details = ArrayList<CoinDetailsEntity>()
                        balanceResponse.coins.forEach { response ->
                            with(response) {
                                val entity = CoinEntity(
                                    id, idx, code, address, balance,
                                    fiatBalance, reservedBalance, reservedFiatBalance, price
                                )
                                coins.add(entity)
                            }
                            with(response.details) {
                                val entity = CoinDetailsEntity(
                                    response.id, txFee, byteFee, scale,
                                    platformSwapFee, platformTradeFee, walletAddress,
                                    gasLimit, gasPrice, convertedTxFee
                                )
                                details.add(entity)
                            }
                        }

                        walletDao.updateBalance(wallet, coins, details)
                    }
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

    private suspend fun processError(throwable: Throwable) {
        val error = when (throwable) {
            is Failure -> throwable
            else -> Failure.ServerError()
        }
        connectionFailure.send(error)
    }

    private suspend fun processErrorMessage(socketResponse: StompSocketResponse) {
        val isTokenExpired = socketResponse.headers[HEADER_MESSAGE_KEY]
            .orEmpty()
            .contains(AUTH_ERROR_MESSAGE)
        if (!isTokenExpired) {
            processError(Failure.ServerError())
            return
        }
        val request = RefreshTokenRequest(preferencesHelper.refreshToken)
        val response = authApi.refereshToken(request).execute()
        val body = response.body()
        val code = response.code()
        when {
            code == HttpURLConnection.HTTP_OK && body != null -> {
                preferencesHelper.processAuthResponse(body)
                disconnect()
                connect()
            }
            code == HttpURLConnection.HTTP_FORBIDDEN || code == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                unlinkHandler.performUnlink()
                disconnect()
            }
        }
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