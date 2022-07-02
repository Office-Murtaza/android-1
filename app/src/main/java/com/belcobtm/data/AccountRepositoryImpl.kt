package com.belcobtm.data

import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.account.mapToDataItem
import com.belcobtm.data.rest.wallet.WalletApiService
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.account.AccountRepository
import com.belcobtm.domain.wallet.item.AccountDataItem

class AccountRepositoryImpl(
    private val daoAccount: AccountDao,
    private val apiService: WalletApiService
) : AccountRepository {

    override suspend fun getAvailableAccounts(): List<AccountDataItem> {
        return (daoAccount.getAvailableAccounts() ?: emptyList()).map { it.mapToDataItem() }
    }

    override suspend fun updateAccountCoinsList(
        accountDataItem: AccountDataItem
    ): Either<Failure, Unit> {
        val toggleCoinStateResult = apiService.toggleCoinState(
            accountDataItem.type.name,
            accountDataItem.isEnabled
        )
        return if (toggleCoinStateResult.isRight) {
            daoAccount.updateIsEnabledByName(accountDataItem.type.name, accountDataItem.isEnabled)
            Either.Right(Unit)
        } else {
            toggleCoinStateResult
        }
    }

}
