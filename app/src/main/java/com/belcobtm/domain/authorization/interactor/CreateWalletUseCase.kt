package com.belcobtm.domain.authorization.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.authorization.AuthorizationRepository
import com.belcobtm.domain.notification.NotificationTokenRepository

//we don't handle it because phone is handled on previous screen
//and coins are hardcoded for now. // TODO:: check this
const val CREATE_ERROR_EMPTY_COINS = 2
const val CREATE_ERROR_MISSED_COINS = 3
const val CREATE_ERROR_PHONE_ALREADY_EXISTS = 4

class CreateWalletUseCase(
    private val repository: AuthorizationRepository,
    private val notificationRepository: NotificationTokenRepository
) : UseCase<Unit, CreateWalletUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.createWallet(
            params.phone,
            params.password,
            params.email,
            notificationRepository.getToken()
        )

    data class Params(val phone: String, val password: String, val email: String)

}
