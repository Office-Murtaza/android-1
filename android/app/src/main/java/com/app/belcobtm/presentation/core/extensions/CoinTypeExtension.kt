package com.app.belcobtm.presentation.core.extensions

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
        CoinType.DOGECOIN.code() -> CoinType.DOGECOIN
        CoinType.DASH.code() -> CoinType.DASH
        else -> null
    }
}

val USDC_UNIT = 1_000_000L

fun CoinType.verboseValue(): String = when (this) {
    CoinType.BITCOIN -> "Bitcoin"
    CoinType.ETHEREUM -> "Ethereum"
    CoinType.BITCOINCASH -> "Bitcoin Cash"
    CoinType.LITECOIN -> "Litecoin"
    CoinType.BINANCE -> "Binance"
    CoinType.TRON -> "Tron"
    CoinType.XRP -> "Ripple"
    CoinType.DOGECOIN -> "Dogecoin"
    CoinType.DASH -> "Dash"
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
    CoinType.DOGECOIN -> "Dogecoin"
    CoinType.DASH -> "Dash"
    else -> ""
}


fun CoinType.unit(): Long = when (this) {
    CoinType.BITCOIN,
    CoinType.BITCOINCASH,
    CoinType.LITECOIN,
    CoinType.DASH,
    CoinType.DOGECOIN,
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


