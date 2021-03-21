package com.app.belcobtm.data.websockets.trade

interface TradesObserver {

    suspend fun connect()

    suspend fun disconnect()

}