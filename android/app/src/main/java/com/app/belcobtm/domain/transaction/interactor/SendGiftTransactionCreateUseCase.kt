package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class SendGiftTransactionCreateUseCase(
    private val repository: TransactionRepository
) :
    UseCase<Unit, SendGiftTransactionCreateUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.sendGift(
        amount = params.amount,
        coinCode = params.coinCode,
        phone = params.phone,
        message = params.message,
        giftId = params.giftId
    )

    data class Params(
        val amount: Double,
        val coinCode: String,
        val phone: String,
        val message: String?,
        val giftId: String?
    )
}