package com.app.belcobtm.data.websockets.base.model

import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.BalanceDataItem

sealed class WalletBalance {
    object NoInfo : WalletBalance()
    data class Balance(val data: BalanceDataItem) : WalletBalance()
    data class Error(val error: Failure) : WalletBalance()
}