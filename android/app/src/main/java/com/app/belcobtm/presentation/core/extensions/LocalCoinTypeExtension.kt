package com.app.belcobtm.presentation.core.extensions

import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType

fun LocalCoinType.resIcon(): Int = when (this) {
    LocalCoinType.BTC -> R.drawable.ic_coin_bitcoin
    LocalCoinType.ETH -> R.drawable.ic_coin_ethereum
    LocalCoinType.BCH -> R.drawable.ic_coin_bitcoin_cash
    LocalCoinType.LTC -> R.drawable.ic_coin_litecoin
    LocalCoinType.BNB -> R.drawable.ic_coin_binance
    LocalCoinType.TRX -> R.drawable.ic_coin_tron
    LocalCoinType.XRP -> R.drawable.ic_coin_ripple
    LocalCoinType.CATM -> R.drawable.ic_coin_catm
    LocalCoinType.USDT -> R.drawable.ic_coin_tether
    LocalCoinType.DASH -> R.drawable.ic_coin_dash
    LocalCoinType.DOGE -> R.drawable.ic_coin_doge
}