package com.app.belcobtm.domain.wallet.interactor.trade

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository

class CreateBuyTradeUseCase(private val repository: WalletRepository) : UseCase<Unit, CreateBuyTradeUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> = repository.tradeBuyCreate(
        params.coinCode,
        params.paymentMethod,
        params.margin,
        params.minLimit,
        params.maxLimit,
        params.terms
    )

    data class Params(
        val coinCode: String,
        val paymentMethod: String,
        val margin: Int,
        val minLimit: Long,
        val maxLimit: Long,
        val terms: String
    )
}