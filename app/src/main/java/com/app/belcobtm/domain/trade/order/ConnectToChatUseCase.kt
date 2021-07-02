package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.data.websockets.chat.ChatObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase

class ConnectToChatUseCase(
    private val chatObserver: ChatObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        chatObserver.connect()
        return Either.Right(Unit)
    }
}