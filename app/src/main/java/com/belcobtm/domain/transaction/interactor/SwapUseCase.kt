package com.belcobtm.domain.transaction.interactor

import com.belcobtm.R
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.presentation.core.provider.string.StringProvider

class SwapUseCase(
    private val repository: TransactionRepository,
    private val locationProvider: LocationProvider,
    private val stringProvider: StringProvider
) : UseCase<Unit, SwapUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> {
        locationProvider.getCurrentLocation()?.let {
            return repository.exchange(
                useMaxAmountFlag = params.useMaxAmountFlag,
                fromCoinAmount = params.coinFromAmount,
                toCoinAmount = params.coinToAmount,
                fromCoin = params.coinFrom,
                coinTo = params.coinTo,
                fee = params.fee,
                fiatAmount = params.fiatAmount,
                transactionPlanItem = params.transactionPlanItem,
                location = it
            )
        }
        return Either.Left(Failure.LocationError(stringProvider.getString(R.string.location_required_on_trade_creation)))
    }

    data class Params(
        val useMaxAmountFlag: Boolean,
        val coinFromAmount: Double,
        val coinToAmount: Double,
        val coinFrom: String,
        val fee: Double,
        val fiatAmount: Double,
        val transactionPlanItem: TransactionPlanItem,
        val coinTo: String
    )
}
