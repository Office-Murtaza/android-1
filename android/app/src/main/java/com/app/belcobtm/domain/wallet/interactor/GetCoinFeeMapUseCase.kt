package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.wallet.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.WalletRepository

class GetCoinFeeMapUseCase(private val repository: WalletRepository) {
    fun getCoinFeeMap(): Map<String, CoinFeeDataItem> = repository.getCoinFeeMap()
}