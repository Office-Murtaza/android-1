package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradePayment

class TradePaymentOptionMapper {

    fun map(payment: PaymentMethodType): TradePayment =
        TradePayment(payment, payment.getIconForPayment(), payment.getTitleForPayment())


}
