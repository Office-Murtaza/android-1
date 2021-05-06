package com.app.belcobtm.data.di

import androidx.lifecycle.LifecycleObserver
import com.app.belcobtm.data.rest.interceptor.BaseInterceptor
import com.app.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.app.belcobtm.data.websockets.base.OkHttpSocketClient
import com.app.belcobtm.data.websockets.base.SocketClient
import com.app.belcobtm.data.websockets.base.model.StompSocketRequest
import com.app.belcobtm.data.websockets.base.model.StompSocketResponse
import com.app.belcobtm.data.websockets.base.serializer.StompRequestSerializer
import com.app.belcobtm.data.websockets.base.serializer.StompRequestSerializer.Companion.STOMP_REQUEST_SERIALIZER_QUALIFIER
import com.app.belcobtm.data.websockets.base.serializer.StompResponseDeserializer
import com.app.belcobtm.data.websockets.base.serializer.StompResponseDeserializer.Companion.STOMP_RESPONSE_DESERIALIZER_QUALIFIER
import com.app.belcobtm.data.websockets.chat.ChatObserver
import com.app.belcobtm.data.websockets.chat.WebSocketChatObserver
import com.app.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.app.belcobtm.data.websockets.chat.serializer.ChatRequestSerializer
import com.app.belcobtm.data.websockets.chat.serializer.ChatRequestSerializer.Companion.CHAT_REQUEST_SERIALIZER_QUALIFIER
import com.app.belcobtm.data.websockets.chat.serializer.ChatResponseDeserializer
import com.app.belcobtm.data.websockets.chat.serializer.ChatResponseDeserializer.Companion.CHAT_RESPONSE_DESERIALIZER_QUALIFIER
import com.app.belcobtm.data.websockets.order.OrdersObserver
import com.app.belcobtm.data.websockets.order.WebSocketOrdersObserver
import com.app.belcobtm.data.websockets.serializer.RequestSerializer
import com.app.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.app.belcobtm.data.websockets.trade.TradesObserver
import com.app.belcobtm.data.websockets.trade.WebSocketTradesObserver
import com.app.belcobtm.data.websockets.transactions.TransactionsObserver
import com.app.belcobtm.data.websockets.transactions.WebSocketTransactionsObserver
import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.data.websockets.wallet.WebSocketWalletObserver
import com.app.belcobtm.data.websockets.wallet.lifecycle.WalletLifecycleObserver
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem
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
            get(), get(), get(),
            get(named(STOMP_REQUEST_SERIALIZER_QUALIFIER)),
            get(named(STOMP_RESPONSE_DESERIALIZER_QUALIFIER)),
            get(), get(), get(authenticatorQualified)
        )
    } bind WalletConnectionHandler::class
    single<TradesObserver> {
        WebSocketTradesObserver(
            get(), get(), get(), get(), get(),
            get(named(STOMP_REQUEST_SERIALIZER_QUALIFIER)),
            get(named(STOMP_RESPONSE_DESERIALIZER_QUALIFIER)),
        )
    }
    single<TransactionsObserver> {
        WebSocketTransactionsObserver(
            get(), get(), get(), get(),
            get(named(STOMP_REQUEST_SERIALIZER_QUALIFIER)),
            get(named(STOMP_RESPONSE_DESERIALIZER_QUALIFIER)),
        )
    }
    single<OrdersObserver> {
        WebSocketOrdersObserver(
            get(), get(), get(), get(), get(),
            get(named(STOMP_REQUEST_SERIALIZER_QUALIFIER)),
            get(named(STOMP_RESPONSE_DESERIALIZER_QUALIFIER))
        )
    }
    single<ChatObserver> {
        WebSocketChatObserver(
            get(), get(), get(),
            get(named(STOMP_REQUEST_SERIALIZER_QUALIFIER)),
            get(named(STOMP_RESPONSE_DESERIALIZER_QUALIFIER)),
            get(named(CHAT_REQUEST_SERIALIZER_QUALIFIER)),
            get(named(CHAT_RESPONSE_DESERIALIZER_QUALIFIER))
        )
    }
    factory<SocketClient> { OkHttpSocketClient(get(WEB_SOCKET_OK_HTTP_CLIENT_QUALIFIER)) }
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
    factory<RequestSerializer<StompSocketRequest>>(named(STOMP_REQUEST_SERIALIZER_QUALIFIER)) {
        StompRequestSerializer()
    }
    factory<ResponseDeserializer<StompSocketResponse>>(named(STOMP_RESPONSE_DESERIALIZER_QUALIFIER)) {
        StompResponseDeserializer()
    }
    factory<RequestSerializer<NewMessageItem>>(named(CHAT_REQUEST_SERIALIZER_QUALIFIER)) {
        ChatRequestSerializer(get())
    }
    factory<ResponseDeserializer<ChatMessageResponse?>>(named(CHAT_RESPONSE_DESERIALIZER_QUALIFIER)) {
        ChatResponseDeserializer(get())
    }
}