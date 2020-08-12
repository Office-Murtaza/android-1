package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository
//we don't handle it because phone is handled on previous screen
//and coins are hardcoded for now.
const val CREATE_ERROR_EMPTY_COINS = 2
const val CREATE_ERROR_MISSED_COINS = 3
const val CREATE_ERROR_PHONE_ALREADY_EXISTS = 4

class CreateWalletUseCase(private val repository: AuthorizationRepository) :
    UseCase<Unit, CreateWalletUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.createWallet(params.phone, params.password)

    data class Params(val phone: String, val password: String)
}