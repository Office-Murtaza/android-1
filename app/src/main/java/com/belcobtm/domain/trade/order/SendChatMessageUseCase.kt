package com.belcobtm.domain.trade.order

import com.belcobtm.data.cloud.auth.CloudAuth
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.data.core.RandomStringGenerator
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.websockets.chat.ChatObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem

class SendChatMessageUseCase(
    private val chatObserver: ChatObserver,
    private val cloudAuth: CloudAuth,
    private val cloudStorage: CloudStorage,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : UseCase<Unit, NewMessageItem>() {

    override suspend fun run(params: NewMessageItem): Either<Failure, Unit> =
        if (params.attachment != null && params.attachmentName != null) {
            try {
                val isLoggedIn = cloudAuth.currentUserExists()
                if (!isLoggedIn) {
                    cloudAuth.authWithToken(sharedPreferencesHelper.firebaseToken)
                }
                cloudStorage.uploadBitmap(params.attachmentName, params.attachment)
                Either.Right(chatObserver.sendMessage(params))
            } catch (e: Exception) {
                e.printStackTrace()
                Either.Left(Failure.NetworkConnection)
            }
        } else {
            Either.Right(chatObserver.sendMessage(params))
        }
}