package com.belcobtm.domain.account

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.item.AccountDataItem

interface AccountRepository {

    suspend fun getAvailableAccounts(): List<AccountDataItem>

    suspend fun updateAccountCoinsList(accountDataItem: AccountDataItem): Either<Failure, Unit>
}
