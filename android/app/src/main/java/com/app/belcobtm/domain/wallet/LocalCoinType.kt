package com.app.belcobtm.domain.wallet

import wallet.core.jni.CoinType

enum class LocalCoinType(val fullName: String, val trustWalletType: CoinType) {
    BTC("Bitcoin", CoinType.BITCOIN),
    ETH("Ethereum", CoinType.ETHEREUM),
    BCH("Bitcoin Cash", CoinType.BITCOINCASH),
    LTC("Litecoin", CoinType.LITECOIN),
    BNB("Binance", CoinType.BINANCE),
    TRX("Tron", CoinType.TRON),
    XRP("Ripple", CoinType.XRP),
    CATM("CATM", CoinType.ETHEREUM)
}