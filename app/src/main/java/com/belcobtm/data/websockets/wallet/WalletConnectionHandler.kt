package com.belcobtm.data.websockets.wallet

import com.belcobtm.domain.Failure
import kotlinx.coroutines.flow.Flow

interface WalletConnectionHandler {

    fun connect()

    fun disconnect()

    fun observeConnectionFailure(): Flow<Failure?>

}