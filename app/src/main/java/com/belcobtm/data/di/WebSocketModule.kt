package com.belcobtm.data.di

import com.belcobtm.data.rest.interceptor.BaseInterceptor
import com.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.belcobtm.data.websockets.base.OkHttpSocketClient
import com.belcobtm.data.websockets.base.SocketClient
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.base.model.StompSocketResponse
import com.belcobtm.data.websockets.base.serializer.StompRequestSerializer
import com.belcobtm.data.websockets.base.serializer.StompRequestSerializer.Companion.STOMP_REQUEST_SERIALIZER_QUALIFIER
import com.belcobtm.data.websockets.base.serializer.StompResponseDeserializer
import com.belcobtm.data.websockets.base.serializer.StompResponseDeserializer.Companion.STOMP_RESPONSE_DESERIALIZER_QUALIFIER
import com.belcobtm.data.websockets.chat.ChatObserver
import com.belcobtm.data.websockets.chat.WebSocketChatObserver
import com.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.belcobtm.data.websockets.chat.serializer.ChatRequestSerializer
import com.belcobtm.data.websockets.chat.serializer.ChatRequestSerializer.Companion.CHAT_REQUEST_SERIALIZER_QUALIFIER
import com.belcobtm.data.websockets.chat.serializer.ChatResponseDeserializer
import com.belcobtm.data.websockets.chat.serializer.ChatResponseDeserializer.Companion.CHAT_RESPONSE_DESERIALIZER_QUALIFIER
import com.belcobtm.data.websockets.manager.SocketManager
import com.belcobtm.data.websockets.manager.WebSocketManager
import com.belcobtm.data.websockets.order.OrdersObserver
import com.belcobtm.data.websockets.order.WebSocketOrdersObserver
import com.belcobtm.data.websockets.serializer.RequestSerializer
import com.belcobtm.data.websockets.serializer.ResponseDeserializer
import com.belcobtm.data.websockets.services.ServicesObserver
import com.belcobtm.data.websockets.services.WebSocketServicesObserver
import com.belcobtm.data.websockets.trade.TradesObserver
import com.belcobtm.data.websockets.trade.WebSocketTradesObserver
import com.belcobtm.data.websockets.transactions.TransactionsObserver
import com.belcobtm.data.websockets.transactions.WebSocketTransactionsObserver
import com.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.belcobtm.data.websockets.wallet.WebSocketWalletObserver
import com.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val WEB_SOCKET_OK_HTTP_CLIENT_QUALIFIER = named("WebSocketOkHttpClient")

val webSocketModule = module {
    single<WalletConnectionHandler> {
        WebSocketWalletObserver(get(), get(), get(), get(), get())
    }
    single<WebSocketManager> {
        SocketManager(
            get(), get(authenticatorQualified), get(), get(), get(), get(),
            get(named(STOMP_REQUEST_SERIALIZER_QUALIFIER)),
            get(named(STOMP_RESPONSE_DESERIALIZER_QUALIFIER)),
        )
    }
    single<TradesObserver> {
        WebSocketTradesObserver(get(), get(), get(), get())
    }
    single<ServicesObserver> {
        WebSocketServicesObserver(get(), get(), get(), get())
    }
    single<TransactionsObserver> {
        WebSocketTransactionsObserver(get(), get(), get(), get())
    }
    single<OrdersObserver> {
        WebSocketOrdersObserver(get(), get(), get(), get())
    }
    single<ChatObserver> {
        WebSocketChatObserver(
            get(), get(), get(),
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