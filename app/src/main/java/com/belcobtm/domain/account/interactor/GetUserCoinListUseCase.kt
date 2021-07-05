package com.belcobtm.domain.account.interactor

import com.belcobtm.domain.account.AccountRepository
import com.belcobtm.domain.wallet.item.AccountDataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class GetUserCoinListUseCase(private val repository: AccountRepository) {

    operator fun invoke(onResult: (List<AccountDataItem>) -> Unit) {
        val job = CoroutineScope(Dispatchers.IO).async { repository.getAccountCoinsList() }
        CoroutineScope(Dispatchers.Main).launch { onResult(job.await()) }
    }
}