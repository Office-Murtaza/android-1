package com.app.belcobtm.util

import wallet.core.jni.CoinType

object TWBitcoinSigHashType {
    const val TWSignatureHashTypeAll = 0x01
    const val TWSignatureHashTypeNone = 0x02
    const val TWSignatureHashTypeSingle = 0x03
    const val TWSignatureHashTypeFork = 0x40
    const val TWSignatureHashTypeAnyoneCanPay = 0x80

    fun getCryptoHash(coinType: CoinType): Int {
        return when (coinType) {
            CoinType.BITCOINCASH -> (TWSignatureHashTypeFork or TWSignatureHashTypeAll)
            else -> TWSignatureHashTypeAll
        }
    }
}