package com.app.belcobtm.data.rest

import com.app.belcobtm.data.rest.wallet.response.BalanceResponse
import com.app.belcobtm.data.rest.wallet.response.mapToDataItem
import com.app.belcobtm.data.sockets.SocketClient
import com.app.belcobtm.data.sockets.SocketMessage
import com.app.belcobtm.data.sockets.SocketMessageListener
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.WalletSocketRepository
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.squareup.moshi.Moshi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.runBlocking

class WalletSocketRepositoryImpl(private val socketClient: SocketClient) : WalletSocketRepository {
    private val balanceEndpoint = "/user/queue/balance"
    private var channel: Channel<Either<Failure, BalanceDataItem>>? = getChannel()
    private var onFailureListener: (Failure) -> Unit = {
        runBlocking {
            channel?.send(Either.Left(it))
        }
    }
    private var onMessageListener = object : SocketMessageListener {
        override fun onMessage(message: SocketMessage) {
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter<BalanceResponse>(BalanceResponse::class.java)
            runBlocking {
                try {
                    channel?.send(Either.Right(adapter.fromJson(message.body)!!.mapToDataItem()))
                } catch (e: Exception) {
                    channel?.send(Either.Left(Failure.MessageError("Wrong JSON: ${message.body}")))
                }
            }
        }
    }

    init {
        socketClient.onFailure = onFailureListener
    }

    override fun close() {
        channel?.close()
        socketClient.disconnect()
    }

    override fun open() {
        if (!socketClient.isConnected()) {
            channel = getChannel()
            socketClient.getTopicHandler(balanceEndpoint)?.removeListener(onMessageListener)
            socketClient.subscribe(balanceEndpoint).addListener(onMessageListener)
            socketClient.connect()
        }
    }

    override suspend fun getBalanceFlow(): Channel<Either<Failure, BalanceDataItem>> {
        return channel ?: getChannel()
    }

    private fun getChannel(): Channel<Either<Failure, BalanceDataItem>> {
        channel?.run {
            close()
        }
        return Channel(BUFFERED)
    }
}