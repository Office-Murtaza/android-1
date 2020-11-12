package com.app.belcobtm.data.websockets.wallet.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletLifecycleObserver(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val connectionHandler: WalletConnectionHandler
) : LifecycleObserver {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        ioScope.launch {
            if (sharedPreferencesHelper.accessToken.isNotEmpty()) {
                connectionHandler.connect()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        ioScope.launch {
            if (sharedPreferencesHelper.accessToken.isNotEmpty()) {
                connectionHandler.disconnect()
            }
        }
    }
}