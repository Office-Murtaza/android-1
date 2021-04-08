package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.data.websockets.chat.ChatObserver
import com.app.belcobtm.domain.trade.order.mapper.ChatMessageMapper
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveChatMessagesUseCase(
    private val chatObserver: ChatObserver,
    private val mapper: ChatMessageMapper
) {

    operator fun invoke(): Flow<List<ChatMessageItem>> =
        chatObserver.observeChatMessages()
            .map { it.map(mapper::map) }
            .flowOn(Dispatchers.Default)
}