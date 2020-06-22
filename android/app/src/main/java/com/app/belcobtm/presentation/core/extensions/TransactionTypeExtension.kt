package com.app.belcobtm.presentation.core.extensions

import com.app.belcobtm.R
import com.app.belcobtm.domain.transaction.type.TransactionType

fun TransactionType.getResText(): Int = when (this) {
    TransactionType.UNKNOWN -> R.string.transaction_type_unknown
    TransactionType.DEPOSIT -> R.string.transaction_type_deposit
    TransactionType.WITHDRAW -> R.string.transaction_type_withdraw
    TransactionType.SEND_GIFT -> R.string.transaction_type_send_gift
    TransactionType.RECEIVE_GIFT -> R.string.transaction_type_receive_gift
    TransactionType.BUY -> R.string.transaction_type_buy
    TransactionType.SELL -> R.string.transaction_type_sell
    TransactionType.MOVE -> R.string.transaction_type_move
    TransactionType.SEND_EXCHANGE -> R.string.transaction_type_send_exchange
    TransactionType.RECEIVE_EXCHANGE -> R.string.transaction_type_receive_exchange
    TransactionType.RESERVE -> R.string.transaction_type_reserve
    TransactionType.RECALL -> R.string.transaction_type_recall
    TransactionType.SELF -> R.string.transaction_type_self
}
