package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository

class SellUseCase(private val repository: WalletRepository) : UseCase<Unit, SellUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.sell(params.coinFrom, params.coinFromAmount)

    data class Params(
        val coinFrom: String,
        val coinFromAmount: Double
    )
}