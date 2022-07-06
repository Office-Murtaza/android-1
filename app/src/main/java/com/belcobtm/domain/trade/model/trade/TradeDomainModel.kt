package com.belcobtm.domain.trade.model.trade

import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.domain.wallet.LocalCoinType

data class TradeDomainModel(
    val id: String,
    val type: TradeType,
    val coin: LocalCoinType,
    val status: TradeStatus,
    val price: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val fiatAmount: Double,
    val feePercent: Double,
    val paymentMethods: List<PaymentMethodType>,
    val terms: String,
    val makerLatitude: Double,
    val makerLongitude: Double,
    val makerId: String,
    val makerUsername: String,
    val makerTotalTrades: Int,
    val makerTradingRate: Double,
    val ordersCount: Int,
    val timestamp: Long,
    var distance: Double = UNDEFINED_DISTANCE
) {

    companion object {

        /**
         * Max value is set because of sorting option.
         * Trades without distance provided should be set to the bottom for distance sorting
         */
        const val UNDEFINED_DISTANCE = Double.MAX_VALUE
    }

}
