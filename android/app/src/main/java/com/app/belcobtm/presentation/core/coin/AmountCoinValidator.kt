package com.app.belcobtm.presentation.core.coin

import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.coin.model.ValidationResult

class AmountCoinValidator {

    fun validateBalance(
        amount: Double,
        coin: CoinDataItem?,
        coinDetails: CoinDetailsDataItem,
        coinList: List<CoinDataItem>
    ): ValidationResult =
        when (coin?.code) {
            LocalCoinType.CATM.name, LocalCoinType.USDC.name -> {
                val balanceCoin = coinList.find { LocalCoinType.ETH.name == it.code }?.balanceCoin
                if (balanceCoin ?: 0.0 < coinDetails.txFee) {
                    ValidationResult.InValid(R.string.withdraw_screen_where_money_libovski)
                } else {
                    ValidationResult.Valid
                }
            }
            else -> {
                if (amount > (coin?.balanceCoin ?: 0.0 - coinDetails.txFee)) {
                    ValidationResult.InValid(R.string.insufficient_balance)
                } else {
                    ValidationResult.Valid
                }
            }
        }
}