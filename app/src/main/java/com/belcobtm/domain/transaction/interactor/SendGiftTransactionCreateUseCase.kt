package com.belcobtm.domain.transaction.interactor

import com.belcobtm.R
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.tools.extensions.withScale

class SendGiftTransactionCreateUseCase(
    private val repository: TransactionRepository,
    private val locationProvider: LocationProvider,
    private val stringProvider: StringProvider
) : UseCase<Unit, SendGiftTransactionCreateUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> {
        locationProvider.getCurrentLocation()?.let {
            return repository.sendGift(
                useMaxAmountFlag = params.useMaxAmountFlag,
                amount = params.amount,
                coinCode = params.coinCode,
                phone = params.phone,
                message = params.message,
                giftId = params.giftId,
                toAddress = params.toAddress,
                price = params.price,
                fee = params.fee,
                feePercent = params.feePercent.toInt(),
                fiatAmount = params.fiatAmount.withScale(),
                transactionPlanItem = params.transactionPlanItem,
                location = it
            )
        }
        return Either.Left(Failure.LocationError(stringProvider.getString(R.string.location_required_on_trade_creation)))
    }

    data class Params(
        val useMaxAmountFlag: Boolean,
        val amount: Double,
        val coinCode: String,
        val phone: String,
        val message: String?,
        val giftId: String?,
        val toAddress: String,
        val price: Double,
        val fee: Double,
        val feePercent: Double,
        val fiatAmount: Double,
        val transactionPlanItem: TransactionPlanItem
    )

}
