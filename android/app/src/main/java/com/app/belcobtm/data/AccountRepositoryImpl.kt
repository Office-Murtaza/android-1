package com.app.belcobtm.data

import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.database.mapToDataItem
import com.app.belcobtm.data.disk.database.mapToEntity
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.account.AccountRepository
import com.app.belcobtm.domain.wallet.item.AccountDataItem

class AccountRepositoryImpl(
    private val daoAccount: AccountDao,
    private val apiService: WalletApiService
) : AccountRepository {

    override suspend fun getAccountCoinsList(): List<AccountDataItem> {
        return (daoAccount.getItemList() ?: emptyList())
            .sortedBy { it.id }.map { it.mapToDataItem() }
    }

    override suspend fun updateAccountCoinsList(
        accountDataItem: AccountDataItem
    ): Either<Failure, Unit> {
        val toggleCoinStateResult = apiService.toggleCoinState(
            accountDataItem.type.name,
            accountDataItem.isEnabled
        )
        return if (toggleCoinStateResult.isRight) {
            daoAccount.updateItem(accountDataItem.mapToEntity())
            Either.Right(Unit)
        } else {
            toggleCoinStateResult
        }
    }
}
