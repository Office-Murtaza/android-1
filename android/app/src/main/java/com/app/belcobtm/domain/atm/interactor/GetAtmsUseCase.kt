package com.app.belcobtm.domain.atm.interactor

import com.app.belcobtm.data.rest.atm.response.AtmResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.atm.AtmRepository

class GetAtmsUseCase(private val atmRepository: AtmRepository): UseCase<AtmResponse, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, AtmResponse> {
        return atmRepository.getAtms()
    }
}