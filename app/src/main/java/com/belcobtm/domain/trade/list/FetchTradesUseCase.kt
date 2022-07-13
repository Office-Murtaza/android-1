package com.belcobtm.domain.trade.list

import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository

class FetchTradesUseCase(
    private val tradeRepository: TradeRepository,
    private val locationProvider: LocationProvider
) : UseCase<Unit, FetchTradesUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> {
        if (params.calculateDistanceEnabled) {
            locationProvider.getCurrentLocation()?.let {
                tradeRepository.sendLocation(it)
            }
        }
        return Either.Right(
            tradeRepository.fetchTrades(params.calculateDistanceEnabled)
        )
    }

    data class Params(val calculateDistanceEnabled: Boolean)
}