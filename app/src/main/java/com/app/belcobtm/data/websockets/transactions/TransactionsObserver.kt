package com.app.belcobtm.data.websockets.transactions

interface TransactionsObserver {

    fun connect()

    fun disconnect()
}