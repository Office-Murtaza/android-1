package com.app.belcobtm.domain.account.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.account.AccountRepository
import com.app.belcobtm.domain.wallet.item.AccountDataItem

class UpdateUserCoinListUseCase(
    private val repository: AccountRepository
) : UseCase<Unit, UpdateUserCoinListUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.updateAccountCoinsList(params.dataItemLocal)

    data class Params(val dataItemLocal: AccountDataItem)
}