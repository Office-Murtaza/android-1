package com.app.belcobtm.data.websockets.chat

import kotlinx.coroutines.flow.Flow

interface ChatObserver {

    suspend fun connect()

    suspend fun observeChatMessages(): Flow<String>

    suspend fun disconnect()
}