package com.app.belcobtm.presentation.features.wallet.trade.recall

import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.presentation.core.item.CoinScreenItem
import com.app.belcobtm.presentation.core.item.mapToScreenItem

class TradeRecallViewModel(
    private val coinDataItem: CoinDataItem,
    private val feeDataItem: CoinFeeDataItem
) : ViewModel() {
    val coinItem: CoinScreenItem = coinDataItem.mapToScreenItem()
    var selectedAmount: Double = 0.0

    fun getMaxValue(): Double = if (coinDataItem.code == LocalCoinType.CATM.name) {
        coinDataItem.balanceCoin
    } else {
        0.0.coerceAtLeast(coinDataItem.balanceCoin - feeDataItem.txFee)
    }
}