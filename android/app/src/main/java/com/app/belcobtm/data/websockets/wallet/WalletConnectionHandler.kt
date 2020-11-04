package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

interface WalletConnectionHandler {

    suspend fun connect(): Either<Failure, Unit>

    suspend fun subscribe(): Either<Failure, Unit>

    suspend fun unsubscribe(): Either<Failure, Unit>

    suspend fun disconnect(): Either<Failure, Unit>

}