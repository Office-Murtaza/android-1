package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository

class SaveSeedUseCase(
    private val repository: AuthorizationRepository
) : UseCase<Unit, String>() {

    override suspend fun run(params: String): Either<Failure, Unit> = repository.saveSeed(params)
}