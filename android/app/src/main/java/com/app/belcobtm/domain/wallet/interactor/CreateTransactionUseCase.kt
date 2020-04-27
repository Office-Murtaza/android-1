package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.CoinDataItem
import com.app.belcobtm.domain.wallet.WalletRepository

class CreateTransactionUseCase(private val repository: WalletRepository) :
    UseCase<String, CreateTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.createTransaction(params.fromCoin, params.fromCoinAmount, params.isNeedSendSms)

    data class Params(
        val fromCoin: CoinDataItem,
        val fromCoinAmount: Double,
        val isNeedSendSms: Boolean = true
    )
}