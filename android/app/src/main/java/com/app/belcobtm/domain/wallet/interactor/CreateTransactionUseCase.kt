package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import wallet.core.jni.CoinType

class CreateTransactionUseCase(private val repository: WalletRepository) :
    UseCase<String, CreateTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.createTransaction(params.fromCoinDb, params.fromCoinCode, params.fromCoinAmount, params.isNeedSendSms)

    data class Params(
        val fromCoinDb: DbCryptoCoin,
        val fromCoinCode: String,
        val fromCoinAmount: Double,
        val isNeedSendSms: Boolean = true
    )
}