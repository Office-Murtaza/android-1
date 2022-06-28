package com.belcobtm.data.websockets.manager

import com.belcobtm.data.websockets.base.model.SocketState
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.base.model.StompSocketResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import kotlinx.coroutines.flow.Flow

interface WebSocketManager {

    fun observeSocketState(): Flow<SocketState>

    suspend fun subscribe(
        destination: String,
        request: StompSocketRequest
    ): Flow<Either<Failure, StompSocketResponse>?>

    suspend fun sendMessage(request: StompSocketRequest)

    suspend fun connect()

    suspend fun disconnect()

}
