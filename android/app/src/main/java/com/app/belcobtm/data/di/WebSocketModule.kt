package com.app.belcobtm.data.di

import androidx.lifecycle.LifecycleObserver
import com.app.belcobtm.data.websockets.base.OkHttpSocketClient
import com.app.belcobtm.data.websockets.base.SocketClient
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.data.websockets.wallet.WebSocketWalletObserver
import com.app.belcobtm.data.websockets.wallet.lifecycle.WalletLifecycleObserver
import com.app.belcobtm.data.websockets.wallet.model.WalletSocketRequest
import com.app.belcobtm.data.websockets.wallet.model.WalletSocketResponse
import com.app.belcobtm.data.websockets.wallet.serializer.WalletRequestSerializer
import com.app.belcobtm.data.websockets.wallet.serializer.WalletResponseDeserializer
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val WALLET_LIFECYCLE_OBSERVER_QUAILIFIER = named("WalletLifecycleObserver")

val webSocketModule = module {
    single<LifecycleObserver>(WALLET_LIFECYCLE_OBSERVER_QUAILIFIER) {
        WalletLifecycleObserver(get(), get())
    }
    single<WalletObserver> {
        WebSocketWalletObserver(get(), get(), get(), get(), get(), get())
    } bind WalletConnectionHandler::class
    single<SocketClient> { OkHttpSocketClient(get()) }
    single<RequestSerializer<WalletSocketRequest>> { WalletRequestSerializer() }
    single<ResponseDeserializer<WalletSocketResponse>> { WalletResponseDeserializer() }
}