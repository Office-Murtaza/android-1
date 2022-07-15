package com.belcobtm.domain.trade.order

import com.belcobtm.domain.Either
import com.belcobtm.domain.map
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.presentation.screens.wallet.trade.order.chat.model.ChatMessageItem
import com.belcobtm.presentation.screens.wallet.trade.order.chat.model.ChatMessageItem.Companion.PARTNER_MESSAGE_TYPE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ObserveMissedMessageCountUseCase(private val tradeRepository: TradeRepository) {

    operator fun invoke(orderId: String): Flow<Int> =
        tradeRepository.observeTradeData()
            .map {
                (it.map { tradeData ->
                    tradeData.orders[orderId]?.chatHistory.orEmpty()
                } as Either.Right<List<ChatMessageItem>>).b
            }.combine(tradeRepository.observeLastSeenMessageTimestamp()) { chat, timestamp ->
                chat.count {
                    !it.isFromHistory && it.timestamp > timestamp && it.type == PARTNER_MESSAGE_TYPE
                }
            }

}
