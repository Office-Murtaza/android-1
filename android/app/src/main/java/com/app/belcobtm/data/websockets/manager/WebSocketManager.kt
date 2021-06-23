package com.app.belcobtm.data.websockets.manager

import com.app.belcobtm.data.websockets.base.model.SocketState
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.base.model.StompSocketResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import kotlinx.coroutines.flow.Flow

interface WebSocketManager {

    fun observeSocketState(): Flow<SocketState>

    suspend fun subscribe(
        destination: String,
        request: StompSocketRequest
    ): Flow<Either<Failure, StompSocketResponse>?>

    suspend fun unsubscribe(destination: String)

    suspend fun sendMessage(request: StompSocketRequest)

    suspend fun connect()

    suspend fun disconnect()
}