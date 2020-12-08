package com.app.belcobtm.presentation.core.coin

import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem

class CoinCodeProvider {
    fun getCoinCode(coinDataItem: CoinDataItem) =
        when (coinDataItem.code) {
            LocalCoinType.CATM.name -> LocalCoinType.ETH.name
            else -> coinDataItem.code
        }
}