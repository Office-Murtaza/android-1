package com.app.belcobtm.data.websockets.wallet

interface WalletConnectionHandler {

    suspend fun connect()

    suspend fun disconnect()

}