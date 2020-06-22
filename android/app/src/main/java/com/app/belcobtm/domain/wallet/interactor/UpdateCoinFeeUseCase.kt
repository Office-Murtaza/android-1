package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem

class UpdateCoinFeeUseCase(
    private val repository: WalletRepository
) :
    UseCase<CoinFeeDataItem, UpdateCoinFeeUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, CoinFeeDataItem> =
        repository.updateCoinFee(params.coinCode)

    data class Params(val coinCode: String)
}