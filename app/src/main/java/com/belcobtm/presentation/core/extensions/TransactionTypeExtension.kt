package com.belcobtm.presentation.core.extensions

import com.belcobtm.R
import com.belcobtm.domain.transaction.type.TransactionType

fun TransactionType.getResText(): Int = when (this) {
    TransactionType.UNKNOWN -> R.string.transaction_type_unknown
    TransactionType.DEPOSIT -> R.string.transaction_type_deposit
    TransactionType.WITHDRAW -> R.string.transaction_type_withdraw
    TransactionType.SEND_TRANSFER -> R.string.transaction_type_send_gift
    TransactionType.RECEIVE_TRANSFER -> R.string.transaction_type_receive_gift
    TransactionType.BUY -> R.string.transaction_type_buy
    TransactionType.SELL -> R.string.transaction_type_sell
    TransactionType.MOVE -> R.string.transaction_type_move
    TransactionType.SWAP_SEND -> R.string.transaction_type_send_exchange
    TransactionType.SWAP_RECEIVE -> R.string.transaction_type_receive_exchange
    TransactionType.RESERVE -> R.string.transaction_type_reserve
    TransactionType.RECALL -> R.string.transaction_type_recall
    TransactionType.SELF -> R.string.transaction_type_self
    TransactionType.STAKE_CREATE -> R.string.transaction_type_stake_create
    TransactionType.STAKE_CANCEL -> R.string.transaction_type_stake_cancel
    TransactionType.STAKE_WITHDRAW -> R.string.transaction_type_stake_withdraw
}
