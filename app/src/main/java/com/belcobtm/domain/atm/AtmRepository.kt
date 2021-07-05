package com.belcobtm.domain.atm

import com.belcobtm.data.rest.atm.response.AtmResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure

interface AtmRepository {
    suspend fun getAtms(): Either<Failure, AtmResponse>
}