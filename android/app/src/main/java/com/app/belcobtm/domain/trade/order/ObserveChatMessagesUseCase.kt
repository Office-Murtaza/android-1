package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.data.websockets.chat.ChatObserver
import com.app.belcobtm.data.websockets.chat.model.ChatMessageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class ObserveChatMessagesUseCase(
    private val chatObserver: ChatObserver
) {

    // TODO add mapper to multi type list
    operator fun invoke(params: Int): Flow<List<ChatMessageResponse>> =
        chatObserver.observeChatMessages()
            .flowOn(Dispatchers.Default)
}