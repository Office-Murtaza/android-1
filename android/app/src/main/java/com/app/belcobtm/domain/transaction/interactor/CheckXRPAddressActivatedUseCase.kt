package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class CheckXRPAddressActivatedUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Boolean, CheckXRPAddressActivatedUseCase.Param>() {

    data class Param(val address: String)

    override suspend fun run(params: Param): Either<Failure, Boolean> {
        return transactionRepository.checkXRPAddressActivated(params.address)
    }
}
