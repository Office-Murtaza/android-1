package com.belcobtm.domain.wallet

import wallet.core.jni.CoinType

enum class LocalCoinType(val fullName: String, val trustWalletType: CoinType) {
    BTC("Bitcoin", CoinType.BITCOIN),
    ETH("Ethereum", CoinType.ETHEREUM),
    BCH("Bitcoin Cash", CoinType.BITCOINCASH),
    LTC("Litecoin", CoinType.LITECOIN),
    BNB("BNB", CoinType.BINANCE),
    TRX("Tron", CoinType.TRON),
    XRP("Ripple", CoinType.XRP),
    USDC("USD Coin", CoinType.ETHEREUM),
    DASH("Dash", CoinType.DASH),
    DOGE("Dogecoin", CoinType.DOGECOIN),
    CATM("Crypto ATM", CoinType.ETHEREUM) // // sometimes is used as default value
}
