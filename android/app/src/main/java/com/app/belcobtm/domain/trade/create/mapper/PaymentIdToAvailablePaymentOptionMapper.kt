package com.app.belcobtm.domain.trade.create.mapper

import com.app.belcobtm.data.model.trade.PaymentOption
import com.app.belcobtm.domain.trade.list.mapper.TradePaymentOptionMapper
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption

class PaymentIdToAvailablePaymentOptionMapper(
    private val mapper: TradePaymentOptionMapper
) {

    fun map(@PaymentOption paymentOption: Int) =
        AvailableTradePaymentOption(mapper.map(paymentOption), selected = false)
}