package com.app.belcobtm.data.websockets.transactions

interface TransactionsObserver {

    suspend fun connect()

    suspend fun disconnect()
}