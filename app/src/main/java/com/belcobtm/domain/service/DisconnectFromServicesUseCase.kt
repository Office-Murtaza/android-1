package com.belcobtm.domain.service

import com.belcobtm.data.websockets.services.ServicesObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class DisconnectFromServicesUseCase(
    private val servicesObserver: ServicesObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(servicesObserver.disconnect())
}