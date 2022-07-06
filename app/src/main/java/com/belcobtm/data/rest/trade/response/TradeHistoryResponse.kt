package com.belcobtm.data.rest.trade.response

import com.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.UserTradeStatsDomainModel
import com.belcobtm.domain.trade.model.order.OrderDomainModel
import com.belcobtm.domain.trade.model.trade.TradeDomainModel
import com.belcobtm.domain.trade.order.mapper.ChatMessageMapper

data class TradeHistoryResponse(
    val makerUsername: String?,
    val makerTradeTotal: Int?,
    val makerTradeRate: Double?,
    val trades: List<TradeDetailsResponse?>?,
    val orders: List<TradeOrderDetailsResponse?>?,
    val messages: List<ChatMessageResponse?>?
) {

    suspend fun mapToDomain(chatMessageMapper: ChatMessageMapper): TradeHistoryDomainModel {
        val chatByOrder = messages?.filterNotNull().orEmpty().groupBy { it.orderId }
        return TradeHistoryDomainModel(
            trades = trades
                ?.mapNotNull { it?.mapToDomain() }
                .orEmpty()
                .associateByTo(HashMap(), TradeDomainModel::id),
            orders = orders
                ?.filterNotNull()
                ?.map { order ->
                    val chatHistory = chatByOrder[order.id].orEmpty()
                        .sortedBy(ChatMessageResponse::timestamp)
                        .map { chatMessageMapper.map(it, isFromHistory = true) }
                    order.mapToDomain(chatHistory)
                }
                .orEmpty()
                .associateByTo(HashMap(), OrderDomainModel::id),
            statistics = UserTradeStatsDomainModel(
                publicId = makerUsername.orEmpty(),
                totalTrades = makerTradeTotal ?: 0,
                tradingRate = makerTradeRate ?: 0.0
            )
        )
    }

}
