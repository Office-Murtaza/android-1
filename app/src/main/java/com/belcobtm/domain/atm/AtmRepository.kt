package com.belcobtm.domain.atm

import com.belcobtm.data.rest.atm.response.AtmAddress
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure

interface AtmRepository {
    suspend fun getAtms(): Either<Failure, List<AtmAddress>>
}