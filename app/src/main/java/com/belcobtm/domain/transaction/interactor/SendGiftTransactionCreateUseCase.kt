package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

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