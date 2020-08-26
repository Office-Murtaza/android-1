package com.app.belcobtm.data.rest.atm

import com.app.belcobtm.data.rest.atm.response.AtmResponse
import com.app.belcobtm.data.rest.authorization.request.CheckCredentialsRequest
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

class AtmApiService(private val atmApi: AtmApi) {

    suspend fun getAtms(): Either<Failure, AtmResponse> = try {
        val request = atmApi.getAtmAddress().await()
        request.body()?.let { Either.Right(it) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        Either.Left(failure)
    }
}