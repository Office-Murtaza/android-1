package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class SendGiftTransactionCreateUseCase(private val repository: TransactionRepository) :
    UseCase<String, SendGiftTransactionCreateUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.sendGiftTransactionCreate(params.phone, params.coinFrom, params.coinFromAmount)

    data class Params(val phone: String, val coinFrom: String, val coinFromAmount: Double)
}