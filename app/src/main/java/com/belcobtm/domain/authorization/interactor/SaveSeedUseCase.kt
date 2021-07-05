package com.belcobtm.domain.authorization.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.authorization.AuthorizationRepository

class SaveSeedUseCase(
    private val repository: AuthorizationRepository
) : UseCase<Unit, String>() {

    override suspend fun run(params: String): Either<Failure, Unit> = repository.saveSeed(params)
}