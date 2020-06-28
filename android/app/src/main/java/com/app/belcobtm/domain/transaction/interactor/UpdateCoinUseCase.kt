package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.AccountDataItem

class UpdateCoinUseCase(private val repository: WalletRepository) : UseCase<Unit, UpdateCoinUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.updateAccount(params.dataItemLocal)

    data class Params(val dataItemLocal: AccountDataItem)
}