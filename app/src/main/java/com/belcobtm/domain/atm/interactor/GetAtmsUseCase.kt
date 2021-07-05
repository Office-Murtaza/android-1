package com.belcobtm.domain.atm.interactor

import com.belcobtm.data.rest.atm.response.AtmResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.atm.AtmRepository

class GetAtmsUseCase(private val atmRepository: AtmRepository): UseCase<AtmResponse, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, AtmResponse> {
        return atmRepository.getAtms()
    }
}