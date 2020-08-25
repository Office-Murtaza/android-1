package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinDataItem

class GetCoinListUseCase(private val walletRepository: WalletRepository) {
    operator fun invoke(): List<CoinDataItem> = walletRepository.getCoinItemList()
}