package com.app.belcobtm.data.di

import androidx.lifecycle.LifecycleObserver
import com.app.belcobtm.data.rest.interceptor.BaseInterceptor
import com.app.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor
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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val WALLET_LIFECYCLE_OBSERVER_QUALIFIER = named("WalletLifecycleObserver")
val WEB_SOCKET_OK_HTTP_CLIENT_QUALIFIER = named("WebSocketOkHttpClient")

val webSocketModule = module {
    single<LifecycleObserver>(WALLET_LIFECYCLE_OBSERVER_QUALIFIER) {
        WalletLifecycleObserver(get(), get())
    }
    single<WalletObserver> {
        WebSocketWalletObserver(
            get(), get(), get(), get(), get(), get(),
            get(), get(authenticatorQualified)
        )
    } bind WalletConnectionHandler::class
    single<SocketClient> { OkHttpSocketClient(get(WEB_SOCKET_OK_HTTP_CLIENT_QUALIFIER)) }
    single<OkHttpClient>(WEB_SOCKET_OK_HTTP_CLIENT_QUALIFIER) {
        OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(get<NoConnectionInterceptor>())
            .addInterceptor(get<BaseInterceptor>())
            .addInterceptor(get<ResponseInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }
    single<RequestSerializer<WalletSocketRequest>> { WalletRequestSerializer() }
    single<ResponseDeserializer<WalletSocketResponse>> { WalletResponseDeserializer() }
}