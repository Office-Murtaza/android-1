package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.data.websockets.wallet.model.WalletBalance
import kotlinx.coroutines.flow.Flow

interface WalletObserver : WalletConnectionHandler {

    fun observe(): Flow<WalletBalance>
}