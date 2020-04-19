package com.app.belcobtm.presentation.core.extensions

import com.app.belcobtm.R
import wallet.core.jni.CoinType
import wallet.core.jni.HDVersion
import wallet.core.jni.Purpose

object CoinTypeExtension {
    fun getTypeByCode(code: String): CoinType? = when (code) {
        CoinType.BITCOIN.code() -> CoinType.BITCOIN
        CoinType.BITCOINCASH.code() -> CoinType.BITCOINCASH
        CoinType.LITECOIN.code() -> CoinType.LITECOIN
        CoinType.BINANCE.code() -> CoinType.BINANCE
        CoinType.ETHEREUM.code() -> CoinType.ETHEREUM
        CoinType.TRON.code() -> CoinType.TRON
        CoinType.XRP.code() -> CoinType.XRP
        else -> null
    }
}

fun CoinType.verboseValue(): String = when (this) {
    CoinType.BITCOIN -> "Bitcoin"
    CoinType.ETHEREUM -> "Ethereum"
    CoinType.BITCOINCASH -> "Bitcoin Cash"
    CoinType.LITECOIN -> "Litecoin"
    CoinType.BINANCE -> "Binance"
    CoinType.TRON -> "Tron"
    CoinType.XRP -> "Ripple"
    else -> ""
}

fun CoinType.code(): String = when (this) {
    CoinType.BITCOIN -> "BTC"
    CoinType.ETHEREUM -> "ETH"
    CoinType.BITCOINCASH -> "BCH"
    CoinType.LITECOIN -> "LTC"
    CoinType.BINANCE -> "BNB"
    CoinType.TRON -> "TRX"
    CoinType.XRP -> "XRP"
    else -> ""
}


fun CoinType.unit(): Long = when (this) {
    CoinType.BITCOIN,
    CoinType.BITCOINCASH,
    CoinType.LITECOIN,
    CoinType.BINANCE -> 100_000_000
    CoinType.ETHEREUM -> 1_000_000_000_000_000_000
    CoinType.TRON, CoinType.XRP -> 1_000_000
    else -> 0
}


fun CoinType.customPurpose(): Purpose = if (this == CoinType.BITCOIN) {
    Purpose.BIP44
} else {
    this.purpose()
}

fun CoinType.customXpubVersion(): HDVersion = if (this == CoinType.BITCOIN) {
    HDVersion.XPUB
} else {
    this.xpubVersion()
}

fun CoinType.resIcon(): Int = when (this) {
    CoinType.BITCOIN -> R.drawable.ic_bitcoin
    CoinType.ETHEREUM -> R.drawable.ic_ethereum
    CoinType.BITCOINCASH -> R.drawable.ic_bitcoin_cash
    CoinType.LITECOIN -> R.drawable.ic_litecoin
    CoinType.BINANCE -> R.drawable.ic_binance
    CoinType.TRON -> R.drawable.ic_tron
    CoinType.XRP -> R.drawable.ic_ripple
    else -> 0
}
