package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import kotlinx.coroutines.flow.Flow

interface WalletObserver : WalletConnectionHandler {

    fun observe(): Flow<BalanceDataItem>

}