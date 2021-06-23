package com.app.belcobtm.domain.trade.order

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.map
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem.Companion.PARTNER_MESSAGE_TYPE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ObserveMissedMessageCountUseCase(private val tradeRepository: TradeRepository) {

    operator fun invoke(orderId: String): Flow<Int> =
        tradeRepository.observeTradeData()
            .map {
                ((it?.map { tradeData ->
                    tradeData.orders[orderId]?.chatHistory.orEmpty()
                } ?: Either.Right(emptyList())) as Either.Right<List<ChatMessageItem>>).b
            }.combine(tradeRepository.observeLastSeenMessageTimestamp()) { chat, timestamp ->
                chat.count {
                    !it.isFromHistory && it.timestamp > timestamp && it.type == PARTNER_MESSAGE_TYPE
                }
            }
}