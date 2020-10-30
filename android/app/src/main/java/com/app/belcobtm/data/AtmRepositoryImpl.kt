package com.app.belcobtm.data

import com.app.belcobtm.data.rest.atm.AtmApiService
import com.app.belcobtm.data.rest.atm.response.AtmResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.atm.AtmRepository

class AtmRepositoryImpl(private val atmApiService: AtmApiService) : AtmRepository {

    override suspend fun getAtms(): Either<Failure, AtmResponse> {
        return atmApiService.getAtms()
    }
}
