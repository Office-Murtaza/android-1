package com.belcobtm.domain.authorization.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.authorization.AuthorizationRepository

class CreateSeedUseCase(private val repository: AuthorizationRepository) : UseCase<String, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, String> = repository.createSeedPhrase()
}