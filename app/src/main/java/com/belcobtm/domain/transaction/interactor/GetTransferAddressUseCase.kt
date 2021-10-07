package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class GetTransferAddressUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<String, GetTransferAddressUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, String> =
        transactionRepository.getTransferAddress(params.phone, params.coinCode)

    data class Params(
        val phone: String,
        val coinCode: String,
    )
}