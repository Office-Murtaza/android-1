package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class CheckXRPAddressActivatedUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Boolean, CheckXRPAddressActivatedUseCase.Param>() {

    data class Param(val address: String)

    override suspend fun run(params: Param): Either<Failure, Boolean> {
        return transactionRepository.checkXRPAddressActivated(params.address)
    }
}
