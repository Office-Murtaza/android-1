package com.belcobtm.domain.trade.order

import com.belcobtm.R
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.features.wallet.trade.order.create.model.TradeOrderItem

class CreateOrderUseCase(
    private val tradeRepository: TradeRepository,
    private val locationProvider: LocationProvider,
    private val stringProvider: StringProvider
) : UseCase<String, TradeOrderItem>() {

    override suspend fun run(params: TradeOrderItem): Either<Failure, String> {
        locationProvider.getCurrentLocation()?.let {
            return tradeRepository.createOrder(params, it)
        }
        return Either.Left(Failure.LocationError(stringProvider.getString(R.string.location_required_on_trade_creation)))
    }
}