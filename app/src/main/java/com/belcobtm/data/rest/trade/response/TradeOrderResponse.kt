package com.belcobtm.data.rest.trade.response

import com.belcobtm.domain.trade.model.order.OrderDomainModel
import com.belcobtm.domain.trade.model.order.OrderStatus
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.screens.wallet.trade.order.chat.model.ChatMessageItem

data class TradeOrderResponse(
    val id: String?,
    val tradeId: String?,
    val coin: String?,
    val status: String?,
    val price: Double?,
    val cryptoAmount: Double?,
    val fiatAmount: Double?,
    val feePercent: Double?,
    val paymentMethods: List<String?>?,
    val terms: String?,
    val resolution: String?,
    val makerUserId: String?,
    val makerUsername: String?,
    val makerTradeTotal: Int?,
    val makerRate: Int?,
    val makerTradeRate: Double?,
    val makerLocation: LocationResponse?,
    val takerUserId: String?,
    val takerUsername: String?,
    val takerTradeTotal: Int?,
    val takerRate: Int?,
    val takerTradeRate: Double?,
    val takerLocation: LocationResponse?,
    val timestamp: Long?,
) {

    fun mapToDomain(
        chatHistory: List<ChatMessageItem> = emptyList()
    ): OrderDomainModel = OrderDomainModel(
        id = id.orEmpty(),
        tradeId = tradeId.orEmpty(),
        coin = LocalCoinType.values().firstOrNull { it.name == coin } ?: LocalCoinType.CATM, // nothing else to make default
        status = OrderStatus.values().firstOrNull { it.name == status } ?: OrderStatus.UNKNOWN,
        cryptoAmount = cryptoAmount ?: 0.0,
        price = price ?: 0.0,
        fiatAmount = fiatAmount ?: 0.0,
        feePercent = feePercent ?: 0.0,
        resolution = resolution.orEmpty(),
        takerRate = takerRate ?: 0,
        takerLatitude = takerLocation?.latitude ?: 0.0,
        takerLongitude = takerLocation?.longitude ?: 0.0,
        takerUserId = takerUserId.orEmpty(),
        takerUsername = takerUsername.orEmpty(),
        takerTradeTotal = takerTradeTotal ?: 0,
        takerTradeRate = takerTradeRate ?: 0.0,
        makerUserId = makerUserId.orEmpty(),
        makerUsername = makerUsername.orEmpty(),
        makerRate = makerRate ?: 0,
        makerLatitude = makerLocation?.latitude ?: 0.0,
        makerLongitude = makerLocation?.longitude ?: 0.0,
        makerTradeTotal = makerTradeTotal ?: 0,
        makerTradeRate = makerTradeRate ?: 0.0,
        timestamp = timestamp ?: 0L,
        terms = terms.orEmpty(),
        chatHistory = chatHistory
    )

}
