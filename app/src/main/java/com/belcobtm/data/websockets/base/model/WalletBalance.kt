package com.belcobtm.data.websockets.base.model

import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.item.BalanceDataItem

sealed class WalletBalance {
    object NoInfo : WalletBalance()
    data class Balance(val data: BalanceDataItem) : WalletBalance()
    data class Error(val error: Failure) : WalletBalance()
}