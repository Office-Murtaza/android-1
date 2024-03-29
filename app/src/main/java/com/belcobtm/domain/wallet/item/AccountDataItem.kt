package com.belcobtm.domain.wallet.item

import com.belcobtm.domain.wallet.LocalCoinType

data class AccountDataItem(
    val type: LocalCoinType,
    val publicKey: String = "",
    val privateKey: String = "",
    val isAvailable: Boolean,
    var isEnabled: Boolean
)
