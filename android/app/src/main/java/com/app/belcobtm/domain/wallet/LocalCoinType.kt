package com.app.belcobtm.domain.wallet

import wallet.core.jni.CoinType

enum class LocalCoinType(val fullName: String, val trustWalletType: CoinType) {
    BTC("Bitcoin", CoinType.BITCOIN),
    ETH("Ethereum", CoinType.ETHEREUM),
    CATM("CATM", CoinType.ETHEREUM),
    BCH("Bitcoin Cash", CoinType.BITCOINCASH),
    LTC("Litecoin", CoinType.LITECOIN),
    BNB("Binance", CoinType.BINANCE),
    TRX("Tron", CoinType.TRON),
    XRP("Ripple", CoinType.XRP),
    USDT("Tether", CoinType.ETHEREUM),
    DOGE("Doge", CoinType.DOGECOIN),
    DASH("Dash", CoinType.DASH),
}