package com.belcobtm.domain.transaction.interactor

import com.belcobtm.R
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.presentation.core.provider.string.StringProvider

class StakeCreateUseCase(
    private val transactionRepository: TransactionRepository,
    private val locationProvider: LocationProvider,
    private val stringProvider: StringProvider
) : UseCase<Unit, StakeCreateUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit>  {
        locationProvider.getCurrentLocation()?.let {
            return transactionRepository.stakeCreate(
                params.coinCode,
                params.cryptoAmount,
                params.feePercent,
                params.fiatAmount,
                params.transactionPlanItem,
                it
            )
        }
        return Either.Left(Failure.LocationError(stringProvider.getString(R.string.location_required_on_trade_creation)))
    }

    data class Params(
        val coinCode: String,
        val cryptoAmount: Double,
        val feePercent: Double,
        val fiatAmount: Double,
        val transactionPlanItem: TransactionPlanItem,
    )
}