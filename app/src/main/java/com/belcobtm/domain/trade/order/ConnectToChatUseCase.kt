package com.belcobtm.domain.trade.order

import com.belcobtm.data.websockets.chat.ChatObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class ConnectToChatUseCase(
    private val chatObserver: ChatObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> {
        chatObserver.connect()
        return Either.Right(Unit)
    }

}
