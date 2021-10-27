package com.belcobtm.data

import com.belcobtm.data.rest.atm.AtmApiService
import com.belcobtm.data.rest.atm.response.AtmAddress
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.atm.AtmRepository

class AtmRepositoryImpl(private val atmApiService: AtmApiService) : AtmRepository {

    override suspend fun getAtms(): Either<Failure, List<AtmAddress>> {
        return atmApiService.getAtms()
    }
}
