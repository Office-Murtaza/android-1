package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.domain.wallet.WalletRepository

class GetCoinDetailsMapUseCase(private val repository: WalletRepository) {
    fun getCoinDetailsMap(): Map<String, CoinDetailsDataItem> = repository.getCoinDetailsMap()
}