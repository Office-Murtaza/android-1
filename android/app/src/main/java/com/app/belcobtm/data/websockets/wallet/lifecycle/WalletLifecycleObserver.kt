package com.app.belcobtm.data.websockets.wallet.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WalletLifecycleObserver(
    private val connectionHandler: WalletConnectionHandler
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        GlobalScope.launch {
            connectionHandler.unsubscribe()
            connectionHandler.disconnect()
        }
    }
}