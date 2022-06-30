package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.R
import com.belcobtm.data.model.trade.PaymentOption
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradePayment

class TradePaymentOptionMapper {

    fun map(@PaymentOption paymentId: Int): TradePayment =
        TradePayment(paymentId, getIconForPayment(paymentId), getTitleForPayment(paymentId))

    private fun getIconForPayment(@PaymentOption payment: Int) =
        when (payment) {
            PaymentOption.CASH -> R.drawable.ic_cash_payment
            PaymentOption.CASH_APP -> R.drawable.ic_cash_in_app_payment
            PaymentOption.PAYONEER -> R.drawable.ic_payoneer_payment
            PaymentOption.PAYPAL -> R.drawable.ic_paypal_payment
            PaymentOption.VENMO -> R.drawable.ic_venmo_payment
            else -> throw RuntimeException("Unknown payment options $payment")
        }

    private fun getTitleForPayment(@PaymentOption payment: Int) =
        when (payment) {
            PaymentOption.CASH -> R.string.create_trade_cash_label
            PaymentOption.CASH_APP -> R.string.create_trade_cash_app_label
            PaymentOption.PAYONEER -> R.string.create_trade_payoneer_label
            PaymentOption.PAYPAL -> R.string.create_trade_paypal_label
            PaymentOption.VENMO -> R.string.create_trade_venmo_label
            else -> throw RuntimeException("Unknown payment options $payment")
        }
}