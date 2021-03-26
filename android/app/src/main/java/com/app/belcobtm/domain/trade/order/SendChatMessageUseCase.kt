package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.data.websockets.chat.ChatObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem

class SendChatMessageUseCase(private val chatObserver: ChatObserver) : UseCase<Unit, NewMessageItem>() {

    override suspend fun run(params: NewMessageItem): Either<Failure, Unit> =
        Either.Right(chatObserver.sendMessage(params))
}