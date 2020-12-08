package com.app.belcobtm.presentation.core.coin

import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import kotlin.math.max

class MinMaxCoinValueProvider {

    fun getMinValue(coin: CoinDataItem, coinDetails: CoinDetailsDataItem) =
        coinDetails.txFee

    fun getMaxValue(coin: CoinDataItem, coinDetails: CoinDetailsDataItem) =
        when (coin.code) {
            LocalCoinType.CATM.name -> coin.balanceCoin
            LocalCoinType.XRP.name -> max(0.0, coin.balanceCoin - coinDetails.txFee - 20)
            else -> max(0.0, coin.balanceCoin - coinDetails.txFee)
        }
}