package com.belcobtm.presentation.tools.extensions

import com.belcobtm.R
import com.belcobtm.domain.wallet.LocalCoinType

fun LocalCoinType.resIcon(): Int = when (this) {
    LocalCoinType.BTC -> R.drawable.ic_coin_bitcoin
    LocalCoinType.ETH -> R.drawable.ic_coin_ethereum
    LocalCoinType.BCH -> R.drawable.ic_coin_bitcoin_cash
    LocalCoinType.LTC -> R.drawable.ic_coin_litecoin
    LocalCoinType.BNB -> R.drawable.ic_coin_bnb
    LocalCoinType.TRX -> R.drawable.ic_coin_tron
    LocalCoinType.XRP -> R.drawable.ic_coin_ripple
    LocalCoinType.CATM -> R.drawable.ic_coin_catm
    LocalCoinType.USDC -> R.drawable.ic_coin_usdc
    LocalCoinType.DASH -> R.drawable.ic_coin_dash
    LocalCoinType.DOGE -> R.drawable.ic_coin_doge
}