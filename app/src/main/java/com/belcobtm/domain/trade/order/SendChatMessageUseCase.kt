package com.belcobtm.domain.trade.order

import android.graphics.Bitmap
import com.belcobtm.data.cloud.auth.CloudAuth
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.data.model.trade.Order
import com.belcobtm.data.websockets.chat.ChatObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.PreferencesInteractor
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.map
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.screens.wallet.trade.order.chat.NewMessageItem

class SendChatMessageUseCase(
    private val chatObserver: ChatObserver,
    private val cloudAuth: CloudAuth,
    private val cloudStorage: CloudStorage,
    private val preferences: PreferencesInteractor,
    private val tradeRepository: TradeRepository,
) : UseCase<Unit, SendChatMessageUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> {
        val order = tradeRepository.getOrder(params.orderId)
        if (order.isLeft) {
            return order.map {}
        }
        val orderData = (order as Either.Right<Order>).b
        val myId = preferences.userId
        return if (params.attachment != null && params.attachmentName != null) {
            try {
                val isLoggedIn = cloudAuth.currentUserExists()
                if (!isLoggedIn) {
                    cloudAuth.authWithToken(preferences.firebaseToken)
                }
                cloudStorage.uploadBitmap(params.attachmentName, params.attachment)
                val messageItem = NewMessageItem(
                    params.orderId, myId, resolveToId(myId, orderData),
                    params.message, params.attachmentName
                )
                Either.Right(chatObserver.sendMessage(messageItem))
            } catch (e: Exception) {
                Either.Left(Failure.NetworkConnection)
            }
        } else {
            val messageItem = NewMessageItem(
                params.orderId, myId, resolveToId(myId, orderData), params.message
            )
            Either.Right(chatObserver.sendMessage(messageItem))
        }
    }

    private fun resolveToId(myId: String, orderData: Order): String =
        if (myId == orderData.makerId) orderData.takerId else orderData.makerId

    data class Params(
        val orderId: String,
        val message: String,
        val attachmentName: String?,
        val attachment: Bitmap?
    )

}
