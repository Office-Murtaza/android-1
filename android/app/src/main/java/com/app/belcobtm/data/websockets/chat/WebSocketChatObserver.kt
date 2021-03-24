package com.app.belcobtm.data.websockets.chat

import kotlinx.coroutines.flow.Flow

class WebSocketChatObserver : ChatObserver {

    // /queue/order-chat

    override suspend fun connect() {
        TODO("Not yet implemented")
    }

    override suspend fun observeChatMessages(): Flow<String> {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect() {
        TODO("Not yet implemented")
    }
}