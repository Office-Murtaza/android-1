package com.app.belcobtm.domain.wallet.item

import com.app.belcobtm.domain.wallet.LocalCoinType

data class AccountDataItem(
    val id: Int,
    val type: LocalCoinType,
    val publicKey: String = "",
    val privateKey: String = ""
) {
    var isEnabled: Boolean = true
}