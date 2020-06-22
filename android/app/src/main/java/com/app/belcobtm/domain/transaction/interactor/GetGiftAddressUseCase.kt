package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class GetGiftAddressUseCase(private val repository: TransactionRepository) :
    UseCase<String, GetGiftAddressUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.getGiftAddress(params.coinFrom, params.phone)

    data class Params(val coinFrom: String, val phone: String)
}