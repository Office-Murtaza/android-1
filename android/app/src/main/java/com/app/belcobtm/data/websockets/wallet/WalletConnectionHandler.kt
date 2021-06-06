package com.app.belcobtm.data.websockets.wallet

import com.app.belcobtm.domain.Failure
import kotlinx.coroutines.flow.Flow

interface WalletConnectionHandler {

    suspend fun connect()

    suspend fun disconnect()

    fun observeConnectionFailure(): Flow<Failure?>

}