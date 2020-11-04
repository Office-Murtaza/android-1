package com.app.belcobtm.data.di

import androidx.lifecycle.LifecycleObserver
import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.data.websockets.wallet.WebsocketWalletObserver
import com.app.belcobtm.data.websockets.wallet.lifecycle.WalletLifecycleObserver
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val WALLET_LIFECYCLE_OBSERVER_QUAILIFIER = named("WalletLifecycleObserver")

val webSocketModule = module {
    single<LifecycleObserver>(WALLET_LIFECYCLE_OBSERVER_QUAILIFIER) { WalletLifecycleObserver(get()) }
    single<WalletObserver> { WebsocketWalletObserver() } bind WalletConnectionHandler::class
}