package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class GetCoinListUseCase(private val repository: WalletRepository) {
    operator fun invoke(onResult: (List<CoinDataItem>) -> Unit) {
        val job = CoroutineScope(Dispatchers.IO).async { repository.getCoinList() }
        CoroutineScope(Dispatchers.Main).launch { onResult(job.await()) }
    }
}