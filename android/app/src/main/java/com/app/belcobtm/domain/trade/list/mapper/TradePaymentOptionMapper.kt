package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.PaymentOption
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradePayment

class TradePaymentOptionMapper {

    fun map(@PaymentOption paymentId: Int): TradePayment =
        TradePayment(paymentId, getIconForPayment(paymentId))

    private fun getIconForPayment(@PaymentOption payment: Int) =
        when (payment) {
            PaymentOption.CASH -> R.drawable.ic_cash_payment
            PaymentOption.CASH_APP -> R.drawable.ic_cash_in_app_payment
            PaymentOption.PAYONEER -> R.drawable.ic_payoneer_payment
            PaymentOption.PAYPAL -> R.drawable.ic_paypal_payment
            PaymentOption.VENMO -> R.drawable.ic_venmo_payment
            else -> throw RuntimeException("Unknown payment options $payment")
        }
}