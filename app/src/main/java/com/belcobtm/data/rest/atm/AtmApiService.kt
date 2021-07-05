package com.belcobtm.data.rest.atm

import com.belcobtm.data.rest.atm.response.AtmResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure

class AtmApiService(private val atmApi: AtmApi) {

    suspend fun getAtms(): Either<Failure, AtmResponse> = try {
        val request = atmApi.getAtmAddress().await()
        request.body()?.let { Either.Right(it) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        Either.Left(failure)
    }
}