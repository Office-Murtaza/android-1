package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import wallet.core.jni.CoinType

class CreateTransactionUseCase(private val repository: WalletRepository) :
    UseCase<String, CreateTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.createTransaction(params.fromCoinCode, params.fromCoinAmount)

    data class Params(val fromCoinCode: String, val fromCoinAmount: Double)
}