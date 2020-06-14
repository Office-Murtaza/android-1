package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.LocalCoinDataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class GetLocalCoinListUseCase(private val repository: WalletRepository) {
    operator fun invoke(onResult: (List<LocalCoinDataItem>) -> Unit) {
        val job = CoroutineScope(Dispatchers.IO).async { repository.getLocalCoinList() }
        CoroutineScope(Dispatchers.Main).launch { onResult(job.await()) }
    }
}