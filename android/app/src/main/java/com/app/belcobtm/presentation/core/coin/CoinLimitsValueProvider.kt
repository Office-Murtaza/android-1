package com.app.belcobtm.presentation.core.coin

import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import kotlin.math.max

class CoinLimitsValueProvider {

    fun getMaxValue(coin: CoinDataItem) = when (coin.code) {
        LocalCoinType.CATM.name -> coin.balanceCoin
        LocalCoinType.XRP.name -> max(0.0, coin.balanceCoin - coin.details.txFee - 20)
        else -> max(0.0, coin.balanceCoin - coin.details.txFee)
    }
}
