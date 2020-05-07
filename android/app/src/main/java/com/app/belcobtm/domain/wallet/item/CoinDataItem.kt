package com.app.belcobtm.domain.wallet.item

import com.app.belcobtm.domain.wallet.LocalCoinType

data class CoinDataItem(
    val type: LocalCoinType,
    val publicKey: String = "",
    val privateKey: String = ""
) {
    var isEnabled: Boolean = true
}