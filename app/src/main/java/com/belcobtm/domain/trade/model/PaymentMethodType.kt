package com.belcobtm.domain.trade.model

import com.belcobtm.R

enum class PaymentMethodType {

    CASH,
    PAYPAL,
    VENMO,
    CASHAPP,
    PAYONEER,
    ZELLE,
    WESTERNUNION,
    MONEYGRAM,
    OTHER;

    fun getIconForPayment() = when (this) {
        CASH -> R.drawable.ic_payment_cash
        CASHAPP -> R.drawable.ic_payment_cashapp
        PAYONEER -> R.drawable.ic_payment_payoneer
        PAYPAL -> R.drawable.ic_payment_paypal
        VENMO -> R.drawable.ic_payment_venmo
        ZELLE -> R.drawable.ic_payment_zelle
        WESTERNUNION -> R.drawable.ic_payment_western_union
        MONEYGRAM -> R.drawable.ic_payment_money_gram
        OTHER -> R.drawable.ic_payment_other
    }

    fun getTitleForPayment() = when (this) {
        CASH -> R.string.create_trade_cash_label
        CASHAPP -> R.string.create_trade_cash_app_label
        PAYONEER -> R.string.create_trade_payoneer_label
        PAYPAL -> R.string.create_trade_paypal_label
        VENMO -> R.string.create_trade_venmo_label
        ZELLE -> R.string.create_trade_zelle_label
        WESTERNUNION -> R.string.create_trade_western_union_label
        MONEYGRAM -> R.string.create_trade_money_gram_label
        OTHER -> R.string.create_trade_other_label
    }

}
