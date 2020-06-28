package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinDataItem

class GetCoinByCodeUseCase(private val walletRepository: WalletRepository) {
    operator fun invoke(coinCode: String): CoinDataItem = walletRepository.getCoinItemByCode(coinCode)
}