package com.app.belcobtm.domain.atm

import com.app.belcobtm.data.rest.atm.response.AtmResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

interface AtmRepository {
    suspend fun getAtms(): Either<Failure, AtmResponse>
}