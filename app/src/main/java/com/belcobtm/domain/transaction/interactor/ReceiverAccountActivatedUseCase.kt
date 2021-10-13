package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class ReceiverAccountActivatedUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Boolean, ReceiverAccountActivatedUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> =
        transactionRepository.receiverAccountActivated(params.coinCode, params.toAddress)

    data class Params(val toAddress: String, val coinCode: String)
}