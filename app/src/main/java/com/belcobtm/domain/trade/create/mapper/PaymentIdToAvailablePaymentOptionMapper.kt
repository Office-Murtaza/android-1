package com.belcobtm.domain.trade.create.mapper

import com.belcobtm.domain.trade.list.mapper.TradePaymentOptionMapper
import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.presentation.screens.wallet.trade.create.model.AvailableTradePaymentOption

class PaymentIdToAvailablePaymentOptionMapper(
    private val mapper: TradePaymentOptionMapper
) {

    fun map(paymentOption: PaymentMethodType) =
        AvailableTradePaymentOption(mapper.map(paymentOption), selected = false)

}
