package com.belcobtm.presentation.core.coin

import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import kotlin.math.max

class CoinLimitsValueProvider {

    fun getMaxValue(coin: CoinDataItem, useReservedBalance: Boolean = false) = when (coin.code) {
        LocalCoinType.CATM.name -> coin.getBalance(useReservedBalance)
        LocalCoinType.XRP.name -> max(0.0, coin.getBalance(useReservedBalance) - coin.details.txFee - 20)
        else -> max(0.0, coin.getBalance(useReservedBalance) - coin.details.txFee)
    }

    private fun CoinDataItem.getBalance(useReservedBalance: Boolean) =
        if (useReservedBalance) reservedBalanceCoin else balanceCoin
}
