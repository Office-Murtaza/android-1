package com.app.belcobtm.domain.socket

import com.app.belcobtm.data.websockets.manager.WebSocketManager
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase

class DisconnectFromSocketUseCase(
    private val webSocketManager: WebSocketManager
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(webSocketManager.disconnect())
}