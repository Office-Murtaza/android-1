package com.app.belcobtm.util

import wallet.core.jni.CoinType
import wallet.core.jni.HDVersion
import wallet.core.jni.Purpose

fun CoinType.getMyCustomPurpose(): Purpose {
    return when (this) {
        CoinType.BITCOIN -> Purpose.BIP44
        else -> this.purpose()
    }
}

fun CoinType.getMyCustomXpubVersion(): HDVersion {
    return when (this) {
        CoinType.BITCOIN -> HDVersion.XPUB
        else -> this.xpubVersion()
    }
}


