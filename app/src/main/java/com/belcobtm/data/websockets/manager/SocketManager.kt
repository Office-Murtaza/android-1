package com.belcobtm.data.websockets.manager

import com.belcobtm.data.core.UnlinkHandler
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.authorization.AuthApi
import com.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.belcobtm.data.websockets.base.SocketClient
import com.belcobtm.data.websockets.base.model.SocketResponse
import com.belcobtm.data.websockets.base.model.SocketState
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.base.model.StompSocketResponse
import com.belcobtm.data.websockets.serializer.RequestSerializer
import com.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceRepository
import com.belcobtm.presentation.core.Endpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.HttpURLConnection
import java.util.concurrent.ConcurrentHashMap

class SocketManager(
    private val socketClient: SocketClient,
    private val authApi: AuthApi,
    private val unlinkHandler: UnlinkHandler,
    private val preferencesHelper: SharedPreferencesHelper,
    private val serviceRepository: ServiceRepository,
    private val walletDao: WalletDao,
    private val serializer: RequestSerializer<StompSocketRequest>,
    private val deserializer: ResponseDeserializer<StompSocketResponse>
) : WebSocketManager {

    companion object {
        private const val MAX_RECONNECT_AMOUNT = 5

        const val ID_HEADER = "id"
        const val AUTH_HEADER = "Authorization"
        const val DESTINATION_HEADER = "destination"
        const val COINS_HEADER = "coins"

        const val ACCEPT_VERSION_HEADER = "accept-version"
        const val ACCEPT_VERSION_VALUE = "1.1"

        const val HEARTBEAT_HEADER = "heart-beat"
        const val HEARTBEAT_VALUE = "1000,1000"

        const val HEADER_MESSAGE_KEY = "message"
        const val AUTH_ERROR_MESSAGE = "Access is denied"
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var reconnectCounter = 0
    private val subscribers: MutableMap<String, MutableStateFlow<Either<Failure, StompSocketResponse>?>> =
        ConcurrentHashMap()
    private val socketState = MutableStateFlow<SocketState>(SocketState.None)

    init {
        ioScope.launch {
            socketClient.observeMessages()
                .collect {
                    when (it) {
                        is SocketResponse.Opened ->
                            onOpened()
                        is SocketResponse.Disconnected ->
                            socketState.value = SocketState.Closed
                        is SocketResponse.Failure -> {
                            if (reconnectCounter <= MAX_RECONNECT_AMOUNT) {
                                reconnectCounter++
                                delay(reconnectCounter * 1000L)
                                connect()
                            }
                        }
                        is SocketResponse.Message ->
                            processMessage(it.content)
                    }
                }
        }
    }

    override fun observeSocketState(): Flow<SocketState> =
        socketState

    override suspend fun subscribe(
        destination: String,
        request: StompSocketRequest
    ): Flow<Either<Failure, StompSocketResponse>?> {
        val flow: MutableStateFlow<Either<Failure, StompSocketResponse>?> = MutableStateFlow(null)
        subscribers[destination] = flow
        withContext(ioScope.coroutineContext) {
            socketClient.sendMessage(serializer.serialize(request))
        }
        return flow
    }

    override suspend fun unsubscribe(destination: String) {
        withContext(ioScope.coroutineContext) {
            val request = StompSocketRequest(
                StompSocketRequest.UNSUBSCRIBE, mapOf(DESTINATION_HEADER to destination)
            )
            socketClient.sendMessage(serializer.serialize(request))
            subscribers.remove(destination)
            if(subscribers.isEmpty()) {
                disconnect()
            }
        }
    }

    override suspend fun sendMessage(request: StompSocketRequest) {
        withContext(ioScope.coroutineContext) {
            withContext(ioScope.coroutineContext) {
                socketClient.sendMessage(serializer.serialize(request))
            }
        }
    }

    override suspend fun connect() {
        withContext(ioScope.coroutineContext) {
            reconnectCounter = 0
            socketClient.connect(Endpoint.SOCKET_URL)
        }
    }

    override suspend fun disconnect() {
        withContext(ioScope.coroutineContext) {
            clearSubscribers()
            socketClient.close(1000)
        }
    }

    private fun clearSubscribers() {
        val currentDestinations = subscribers.keys.toCollection(ArrayList())
        subscribers.clear()
        currentDestinations.forEach { destination ->
            val request = StompSocketRequest(
                StompSocketRequest.UNSUBSCRIBE, mapOf(DESTINATION_HEADER to destination)
            )
            socketClient.sendMessage(serializer.serialize(request))
        }
    }

    private suspend fun processMessage(content: String) {
        val response = deserializer.deserialize(content)
        when (response.status) {
            StompSocketResponse.CONNECTED -> {
                // re-emit connected state
                socketState.value = SocketState.None
                socketState.value = SocketState.Connected
            }
            StompSocketResponse.ERROR -> {
                processErrorMessage(response)
            }
            StompSocketResponse.CONTENT -> {
                subscribers[response.headers[DESTINATION_HEADER]]?.value = Either.Right(response)
            }
        }
    }

    private fun processError(destination: String?, throwable: Throwable) {
        destination ?: return
        val error = when (throwable) {
            is Failure -> throwable
            else -> Failure.ServerError()
        }
        subscribers[destination]?.value = Either.Left(error)
    }

    private fun onOpened() {
        val request = StompSocketRequest(
            StompSocketRequest.CONNECT, mapOf(
                ACCEPT_VERSION_HEADER to ACCEPT_VERSION_VALUE,
                AUTH_HEADER to preferencesHelper.accessToken,
                HEARTBEAT_HEADER to HEARTBEAT_VALUE
            )
        )
        socketClient.sendMessage(serializer.serialize(request))
    }

    private suspend fun processErrorMessage(socketResponse: StompSocketResponse) {
        val isTokenExpired = socketResponse.headers[HEADER_MESSAGE_KEY]
            .orEmpty()
            .contains(AUTH_ERROR_MESSAGE)
        if (!isTokenExpired) {
            processError(socketResponse.headers[DESTINATION_HEADER], Failure.ServerError())
            return
        }
        clearSubscribers()
        val request = RefreshTokenRequest(preferencesHelper.refreshToken)
        val response = authApi.refereshToken(request).execute()
        val body = response.body()
        val code = response.code()
        when {
            code == HttpURLConnection.HTTP_OK && body != null -> {
                walletDao.updateBalance(body.balance)
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
}