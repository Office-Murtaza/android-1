package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository
import com.app.belcobtm.domain.notification.NotificationTokenRepository

const val RECOVER_ERROR_EMPTY_COINS = 2
const val RECOVER_ERROR_MISSED_COINS = 3
const val RECOVER_ERROR_PHONE_DOESNT_EXISTS = 4
const val RECOVER_ERROR_INCORRECT_PASSWORD = 5
const val RECOVER_ERROR_SEED_PHRASE = 6

class RecoverWalletUseCase(
    private val repository: AuthorizationRepository,
    private val notificationRepository: NotificationTokenRepository
) : UseCase<Unit, RecoverWalletUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.recoverWallet(
            params.seed, params.phone, params.password, notificationRepository.getToken()
        )

    data class Params(val seed: String, val phone: String, val password: String)
}