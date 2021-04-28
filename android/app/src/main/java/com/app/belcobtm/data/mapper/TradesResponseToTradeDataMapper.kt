package com.app.belcobtm.data.mapper

import com.app.belcobtm.data.model.trade.Order
import com.app.belcobtm.data.model.trade.Trade
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.UserTradeStatistics
import com.app.belcobtm.data.rest.trade.response.TradesResponse
import com.app.belcobtm.data.websockets.chat.model.ChatMessageResponse
import com.app.belcobtm.domain.trade.order.mapper.ChatMessageMapper
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem

class TradesResponseToTradeDataMapper(
    private val orderMapper: OrderResponseToOrderMapper,
    private val tradeMapper: TradeResponseToTradeMapper,
    private val chatMessageMapper: ChatMessageMapper
) {

    suspend fun map(response: TradesResponse): TradeData =
        with(response) {
            val chatByOrder = response.messages.groupBy(ChatMessageResponse::orderId)
            TradeData(
                trades.map(tradeMapper::map).associateByTo(HashMap(), Trade::id),
                orders.map { order ->
                    val chatHistory: List<ChatMessageItem> = chatByOrder[order.id].orEmpty()
                        .sortedBy(ChatMessageResponse::timestamp)
                        .map { chatMessageMapper.map(it) }
                        .toList()
                    orderMapper.map(order, chatHistory)
                }
                    .associateByTo(HashMap(), Order::id),
                mapStatistic(this)
            )
        }

    private fun mapStatistic(response: TradesResponse): UserTradeStatistics =
        with(response) {
            UserTradeStatistics(makerPublicId, makerStatus, makerTotalTrades, makerTradingRate)
        }

}