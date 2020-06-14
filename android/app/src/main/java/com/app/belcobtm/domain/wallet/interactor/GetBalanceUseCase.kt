package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.BalanceDataItem

class GetBalanceUseCase(private val repository: WalletRepository) : UseCase<BalanceDataItem, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, BalanceDataItem> = repository.getBalanceItem()
}