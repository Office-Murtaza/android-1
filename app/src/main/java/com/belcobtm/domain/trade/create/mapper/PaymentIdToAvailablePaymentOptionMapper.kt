package com.belcobtm.domain.trade.create.mapper

import com.belcobtm.data.model.trade.PaymentOption
import com.belcobtm.domain.trade.list.mapper.TradePaymentOptionMapper
import com.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption

class PaymentIdToAvailablePaymentOptionMapper(
    private val mapper: TradePaymentOptionMapper
) {

    fun map(@PaymentOption paymentOption: Int) =
        AvailableTradePaymentOption(mapper.map(paymentOption), selected = false)
}