package com.belcobtm.presentation.core.coin

import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode

class CoinCodeProvider {

    fun getCoinCode(coinDataItem: CoinDataItem) = getCoinCode(coinDataItem.code)

    fun getCoinCode(coinCode: String) = when (coinCode.isEthRelatedCoinCode()) {
        true -> LocalCoinType.ETH.name
        false -> coinCode
    }
}
