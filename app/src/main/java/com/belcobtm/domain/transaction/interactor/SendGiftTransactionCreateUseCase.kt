package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.presentation.core.extensions.toStringCoin

class SendGiftTransactionCreateUseCase(
    private val repository: TransactionRepository
) : UseCase<Unit, SendGiftTransactionCreateUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> = repository.sendGift(
        useMaxAmountFlag = params.useMaxAmountFlag,
        amount = params.amount,
        coinCode = params.coinCode,
        phone = params.phone,
        message = params.message,
        giftId = params.giftId,
        toAddress = params.toAddress,
        fee = params.fee,
        feePercent = params.feePercent.toInt(),
        fiatAmount = params.fiatAmount.toStringCoin().toDouble(),
        transactionPlanItem = params.transactionPlanItem
    )

    data class Params(
        val useMaxAmountFlag: Boolean,
        val amount: Double,
        val coinCode: String,
        val phone: String,
        val message: String?,
        val giftId: String?,
        val toAddress: String,
        val fee: Double,
        val feePercent: Double,
        val fiatAmount: Double,
        val transactionPlanItem: TransactionPlanItem,
    )
}