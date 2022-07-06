package com.belcobtm.data.rest.trade.response

import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.domain.trade.model.trade.TradeDomainModel
import com.belcobtm.domain.trade.model.trade.TradeStatus
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.domain.wallet.LocalCoinType

data class TradeResponse(
    val id: String?,
    val type: String?,
    val coin: String?,
    val status: String?,
    val price: Double?,
    val minLimit: Double?,
    val maxLimit: Double?,
    val fiatAmount: Double?,
    val feePercent: Double?,
    val paymentMethods: List<String?>?,
    val terms: String?,
    val latitude: Double?,
    val longitude: Double?
) {

    fun mapToDomain(): TradeDomainModel = TradeDomainModel(
        id = id.orEmpty(),
        type = TradeType.values().firstOrNull { it.name == type } ?: TradeType.UNKNOWN,
        coin = LocalCoinType.values().firstOrNull { it.name == coin } ?: LocalCoinType.CATM, // nothing else to make default
        status = TradeStatus.values().firstOrNull { it.name == status } ?: TradeStatus.UNKNOWN,
        price = price ?: 0.0,
        minLimit = minLimit ?: 0.0,
        maxLimit = maxLimit ?: 0.0,
        fiatAmount = fiatAmount ?: 0.0,
        feePercent = feePercent ?: 0.0,
        paymentMethods = paymentMethods?.mapNotNull {
            PaymentMethodType.values().firstOrNull { type -> type.name == it }
        }.orEmpty(),
        terms = terms.orEmpty(),
        timestamp = 0L,
        ordersCount = 0,
        makerId = "",
        makerUsername = "",
        makerLatitude = latitude ?: 0.0,
        makerLongitude = longitude ?: 0.0,
        makerTotalTrades = 0,
        makerTradingRate = 0.0
    )

}
