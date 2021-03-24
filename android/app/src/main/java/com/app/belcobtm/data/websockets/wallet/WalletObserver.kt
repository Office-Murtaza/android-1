package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.data.websockets.base.model.WalletBalance
import kotlinx.coroutines.channels.ReceiveChannel

interface WalletObserver : WalletConnectionHandler {

    fun observe(): ReceiveChannel<WalletBalance>
}