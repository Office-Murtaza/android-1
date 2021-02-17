package com.app.belcobtm.domain.account

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.AccountDataItem

interface AccountRepository {

    suspend fun getAccountCoinsList(): List<AccountDataItem>

    suspend fun updateAccountCoinsList(accountDataItem: AccountDataItem): Either<Failure, Unit>
}
