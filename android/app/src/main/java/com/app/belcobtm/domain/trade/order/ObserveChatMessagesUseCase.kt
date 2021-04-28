package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.data.websockets.chat.ChatObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.order.mapper.ChatMessageMapper
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveChatMessagesUseCase(
    private val chatObserver: ChatObserver,
    private val tradeRepository: TradeRepository,
    private val mapper: ChatMessageMapper
) {

    operator fun invoke(orderId: String): Flow<List<ListItem>> =
        observeChatHistory(orderId).combine(observeNewMessages()) { history, newMessages ->
            history + newMessages
        }.flowOn(Dispatchers.Default)

    private fun observeChatHistory(orderId: String): Flow<List<ListItem>> = tradeRepository.observeTradeData()
        .map {
            ((it?.map { tradeData ->
                tradeData.orders.getValue(orderId).chatHistory
            } ?: Either.Right(emptyList())) as Either.Right<List<ListItem>>).b
        }

    private fun observeNewMessages() =
        chatObserver.observeChatMessages()
            .map { messages -> messages.map { mapper.map(it) } }
}