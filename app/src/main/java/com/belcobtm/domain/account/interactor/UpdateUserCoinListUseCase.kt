package com.belcobtm.domain.account.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.account.AccountRepository
import com.belcobtm.domain.wallet.item.AccountDataItem

class UpdateUserCoinListUseCase(
    private val repository: AccountRepository
) : UseCase<Unit, UpdateUserCoinListUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.updateAccountCoinsList(params.dataItemLocal)

    data class Params(val dataItemLocal: AccountDataItem)
}