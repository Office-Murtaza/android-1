package com.app.belcobtm.data.websockets.order

interface OrdersObserver {

    suspend fun connect()

    suspend fun disconnect()
}