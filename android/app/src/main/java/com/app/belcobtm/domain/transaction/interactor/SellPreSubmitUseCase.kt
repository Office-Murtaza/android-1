package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.SellPreSubmitDataItem

class SellPreSubmitUseCase(private val repository: TransactionRepository) :
    UseCase<SellPreSubmitDataItem, SellPreSubmitUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, SellPreSubmitDataItem> =
        repository.sellPreSubmit(params.smsCode, params.coinFrom, params.cryptoAmount, params.toUsdAmount)

    data class Params(
        val smsCode: String,
        val coinFrom: String,
        val cryptoAmount: Double,
        val toUsdAmount: Int
    )
}