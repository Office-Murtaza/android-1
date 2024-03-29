package com.belcobtm.presentation.tools.extensions

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

const val USDC_UNIT = 1_000_000L

fun CoinType.code(): String = when (this) {
    CoinType.BITCOIN -> "BTC"
    CoinType.ETHEREUM -> "ETH"
    CoinType.BITCOINCASH -> "BCH"
    CoinType.LITECOIN -> "LTC"
    CoinType.BINANCE -> "BNB"
    CoinType.TRON -> "TRX"
    CoinType.XRP -> "XRP"
    CoinType.DOGECOIN -> "DOGE"
    CoinType.DASH -> "DASH"
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


