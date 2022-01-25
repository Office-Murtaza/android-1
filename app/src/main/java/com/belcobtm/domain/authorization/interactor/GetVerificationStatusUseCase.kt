package com.belcobtm.domain.authorization.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.authorization.AuthorizationRepository
import com.belcobtm.domain.settings.type.VerificationStatus

class GetVerificationStatusUseCase(
    private val repository: AuthorizationRepository
) : UseCase<VerificationStatus, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, VerificationStatus> = repository.getVerificationStatus()
}