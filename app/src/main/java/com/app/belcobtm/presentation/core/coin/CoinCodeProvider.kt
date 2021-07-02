package com.app.belcobtm.presentation.core.coin

import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.isEthRelatedCoinCode

class CoinCodeProvider {

    fun getCoinCode(coinDataItem: CoinDataItem) = getCoinCode(coinDataItem.code)

    fun getCoinCode(coinCode: String) = when (coinCode.isEthRelatedCoinCode()) {
        true -> LocalCoinType.ETH.name
        false -> coinCode
    }
}
