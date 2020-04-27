package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository

class WithdrawUseCase(private val repository: WalletRepository) : UseCase<Unit, WithdrawUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.withdraw(
        params.smsCode,
        params.hash,
        params.coinFrom,
        params.coinFromAmount
    )

    data class Params(
        val smsCode: String,
        val hash: String,
        val coinFrom: String,
        val coinFromAmount: Double
    )
}