package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.R
import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradePayment

class TradePaymentOptionMapper {

    fun map(paymentId: PaymentMethodType): TradePayment =
        TradePayment(paymentId, getIconForPayment(paymentId), getTitleForPayment(paymentId))

    private fun getIconForPayment(payment: PaymentMethodType) =
        when (payment) {
            PaymentMethodType.CASH -> R.drawable.ic_cash_payment
            PaymentMethodType.CASHAPP -> R.drawable.ic_cash_in_app_payment
            PaymentMethodType.PAYONEER -> R.drawable.ic_payoneer_payment
            PaymentMethodType.PAYPAL -> R.drawable.ic_paypal_payment
            PaymentMethodType.VENMO -> R.drawable.ic_venmo_payment
            PaymentMethodType.ZELLE,
            PaymentMethodType.WESTERNUNION,
            PaymentMethodType.MONEYGRAM,
            PaymentMethodType.OTHER -> R.drawable.ic_cash_payment
        }

    private fun getTitleForPayment(payment: PaymentMethodType) =
        when (payment) {
            PaymentMethodType.CASH -> R.string.create_trade_cash_label
            PaymentMethodType.CASHAPP -> R.string.create_trade_cash_app_label
            PaymentMethodType.PAYONEER -> R.string.create_trade_payoneer_label
            PaymentMethodType.PAYPAL -> R.string.create_trade_paypal_label
            PaymentMethodType.VENMO -> R.string.create_trade_venmo_label
            PaymentMethodType.ZELLE,
            PaymentMethodType.WESTERNUNION,
            PaymentMethodType.MONEYGRAM,
            PaymentMethodType.OTHER -> R.string.create_trade_other_label
        }

}
