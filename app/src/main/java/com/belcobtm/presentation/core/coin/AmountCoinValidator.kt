package com.belcobtm.presentation.core.coin

import com.belcobtm.R
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.coin.model.ValidationResult

class AmountCoinValidator {

    fun validateBalance(
        amount: Double,
        coin: CoinDataItem?,
        coinList: List<CoinDataItem>
    ): ValidationResult =
        when (coin?.code) {
            LocalCoinType.CATM.name, LocalCoinType.USDC.name -> {
                val balanceCoin = coinList.find { LocalCoinType.ETH.name == it.code }?.balanceCoin
                if (balanceCoin ?: 0.0 < coin.details.txFee) {
                    ValidationResult.InValid(R.string.withdraw_screen_where_money_libovski)
                } else {
                    ValidationResult.Valid
                }
            }
            else -> {
                val txFee = coin?.details?.txFee ?: 0.0
                if (amount > (coin?.balanceCoin ?: 0.0 - txFee)) {
                    ValidationResult.InValid(R.string.insufficient_balance)
                } else {
                    ValidationResult.Valid
                }
            }
        }
}