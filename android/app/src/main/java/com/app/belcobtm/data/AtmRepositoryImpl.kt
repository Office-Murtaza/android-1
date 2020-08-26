package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.rest.atm.AtmApiService
import com.app.belcobtm.data.rest.atm.response.AtmResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.atm.AtmRepository

class AtmRepositoryImpl(
    private val atmApiService: AtmApiService,
    private val networkUtils: NetworkUtils
) : AtmRepository {

    override suspend fun getAtms(): Either<Failure, AtmResponse> {
        return if (networkUtils.isNetworkAvailable()) {
            atmApiService.getAtms()
        } else {
            Either.Left(Failure.NetworkConnection)
        }
    }
}