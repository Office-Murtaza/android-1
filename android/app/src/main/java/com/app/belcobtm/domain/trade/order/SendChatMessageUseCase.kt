package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.data.cloud.storage.CloudStorage
import com.app.belcobtm.data.websockets.chat.ChatObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem

class SendChatMessageUseCase(
    private val chatObserver: ChatObserver,
    private val cloudStorage: CloudStorage
) : UseCase<Unit, NewMessageItem>() {

    override suspend fun run(params: NewMessageItem): Either<Failure, Unit> =
        if (params.attachment != null && params.attachmentName != null) {
            try {
                // TODO test properly
                cloudStorage.uploadBitmap(params.attachmentName, params.attachment)
                Either.Right(chatObserver.sendMessage(params))
            } catch (e: Exception) {
                Either.Left(Failure.NetworkConnection)
            }
        } else {
            Either.Right(chatObserver.sendMessage(params))
        }
}