package com.belcobtm.presentation.tools.extensions

import com.belcobtm.R
import com.belcobtm.domain.transaction.type.TransactionType

fun TransactionType.getResText(): Int = when (this) {
    TransactionType.DEPOSIT -> R.string.transaction_type_deposit
    TransactionType.WITHDRAW -> R.string.transaction_type_withdraw
    TransactionType.SEND_TRANSFER -> R.string.transaction_type_send_gift
    TransactionType.RECEIVE_TRANSFER -> R.string.transaction_type_receive_gift
    TransactionType.ATM_SELL -> R.string.transaction_type_buy
    TransactionType.ATM_BUY -> R.string.transaction_type_sell
    TransactionType.MOVE -> R.string.transaction_type_move
    TransactionType.SEND_SWAP -> R.string.transaction_type_send_exchange
    TransactionType.RECEIVE_SWAP -> R.string.transaction_type_receive_exchange
    TransactionType.RESERVE -> R.string.transaction_type_reserve
    TransactionType.RECALL -> R.string.transaction_type_recall
    TransactionType.SELF -> R.string.transaction_type_self
    TransactionType.CREATE_STAKE -> R.string.transaction_type_stake_create
    TransactionType.CANCEL_STAKE -> R.string.transaction_type_stake_cancel
    TransactionType.WITHDRAW_STAKE -> R.string.transaction_type_stake_withdraw
    TransactionType.UNKNOWN -> R.string.transaction_type_unknown
    else -> R.string.transaction_type_unknown
}
