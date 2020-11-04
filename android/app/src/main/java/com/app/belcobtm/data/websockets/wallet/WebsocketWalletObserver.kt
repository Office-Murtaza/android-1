package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class WebsocketWalletObserver : WalletObserver {
    private val balance = Channel<BalanceDataItem>(Channel.CONFLATED)

    override fun observe(): Flow<BalanceDataItem> =
        balance.consumeAsFlow()

    override suspend fun connect(): Either<Failure, Unit> {
        return Either.Left(Failure.MessageError("Not implemented"))
    }

    override suspend fun subscribe(): Either<Failure, Unit> {
        return Either.Left(Failure.MessageError("Not implemented"))
    }

    override suspend fun unsubscribe(): Either<Failure, Unit> {
        return Either.Left(Failure.MessageError("Not implemented"))
    }

    override suspend fun disconnect(): Either<Failure, Unit> {
        return Either.Left(Failure.MessageError("Not implemented"))
    }
}