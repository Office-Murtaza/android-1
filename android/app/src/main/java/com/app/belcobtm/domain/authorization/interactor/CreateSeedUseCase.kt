package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.authorization.AuthorizationRepository

class CreateSeedUseCase(private val repository: AuthorizationRepository) : UseCase<String, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, String> = repository.createSeedPhrase()
}