package com.app.belcobtm.domain.wallet

import wallet.core.jni.CoinType

data class CoinDataItem(
    val type: CoinType,
    val code: String = "",
    val codeId: Int = -1,
    val publicKey: String = "",
    val privateKey: String = "",
    val isVisible: Boolean = true
)