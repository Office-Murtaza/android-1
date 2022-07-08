package com.belcobtm.domain.trade.create

import com.belcobtm.R
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.domain.trade.model.CreateTradeDomainModel

class CreateTradeUseCase(
    private val tradeRepository: TradeRepository,
    private val locationProvider: LocationProvider,
    private val stringProvider: StringProvider
) : UseCase<Unit, CreateTradeDomainModel>() {

    override suspend fun run(params: CreateTradeDomainModel): Either<Failure, Unit> {
        locationProvider.getCurrentLocation()?.let {
            return tradeRepository.createTrade(params, it)
        }
        return Either.Left(Failure.LocationError(stringProvider.getString(R.string.location_required_on_trade_creation)))
    }
}