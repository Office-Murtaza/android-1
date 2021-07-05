package com.belcobtm.domain.socket

import com.belcobtm.data.websockets.manager.WebSocketManager
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class DisconnectFromSocketUseCase(
    private val webSocketManager: WebSocketManager
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(webSocketManager.disconnect())
}