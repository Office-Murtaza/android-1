package com.belcobtm.data.rest.trade.response

import com.belcobtm.domain.trade.model.order.OrderDomainModel
import com.belcobtm.domain.trade.model.order.OrderStatus
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.screens.wallet.trade.order.chat.model.ChatMessageItem

data class TradeOrderResponse(
    val id: String?,
    val tradeId: String?,
    val status: String?,
    val cryptoAmount: Double?,
    val price: Double?,
    val fiatAmount: Double?,
    val feePercent: Double?,
    val rate: Int?,
    val resolution: String?,
    val latitude: Double?,
    val longitude: Double?
) {

    fun mapToDomain(
        coin: LocalCoinType,
        chatHistory: List<ChatMessageItem> = emptyList()
    ): OrderDomainModel = OrderDomainModel(
        id = id.orEmpty(),
        tradeId = tradeId.orEmpty(),
        coin = coin,
        status = OrderStatus.values().firstOrNull { it.name == status } ?: OrderStatus.UNKNOWN,
        cryptoAmount = cryptoAmount ?: 0.0,
        price = price ?: 0.0,
        fiatAmount = fiatAmount ?: 0.0,
        feePercent = feePercent ?: 0.0,
        resolution = resolution.orEmpty(),
        takerRate = rate ?: 0,
        takerLatitude = latitude ?: 0.0,
        takerLongitude = longitude ?: 0.0,
        takerUserId = "",
        takerUsername = "",
        takerTradeTotal = 0,
        takerTradeRate = 0.0,
        makerUserId = "",
        makerUsername = "",
        makerRate = 0,
        makerLatitude = 0.0,
        makerLongitude = 0.0,
        makerTradeTotal = 0,
        makerTradeRate = 0.0,
        timestamp = 0L,
        terms = "",
        chatHistory = chatHistory
    )

}
