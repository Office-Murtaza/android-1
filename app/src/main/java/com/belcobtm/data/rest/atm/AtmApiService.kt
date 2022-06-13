package com.belcobtm.data.rest.atm

import com.belcobtm.data.rest.atm.response.AtmAddress
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure

class AtmApiService(private val atmApi: AtmApi) {

    suspend fun getAtms(): Either<Failure, List<AtmAddress>> = try {
        val request = atmApi.getAtmAddress()
        request.body()?.let { Either.Right(it) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        Either.Left(failure)
    }
}