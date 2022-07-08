package com.belcobtm.domain.transaction.type

import com.belcobtm.R

enum class TransactionType {
    DEPOSIT,
    WITHDRAW,
    SEND_TRANSFER,
    RECEIVE_TRANSFER,
    ATM_BUY,
    ATM_SELL,
    MOVE,
    SEND_SWAP,
    RECEIVE_SWAP,
    RESERVE,
    RECALL,
    SELF,
    CREATE_STAKE,
    CANCEL_STAKE,
    WITHDRAW_STAKE,
    BUY,
    SELL,
    UNKNOWN; // exists only on client to handle null

    fun getResText(): Int = when (this) {
        DEPOSIT -> R.string.transaction_type_deposit
        WITHDRAW -> R.string.transaction_type_withdraw
        SEND_TRANSFER -> R.string.transaction_type_send_transfer
        RECEIVE_TRANSFER -> R.string.transaction_type_receive_transfer
        ATM_BUY -> R.string.transaction_type_atm_buy
        ATM_SELL -> R.string.transaction_type_atm_sell
        MOVE -> R.string.transaction_type_move
        SEND_SWAP -> R.string.transaction_type_send_swap
        RECEIVE_SWAP -> R.string.transaction_type_receive_swap
        RESERVE -> R.string.transaction_type_reserve
        RECALL -> R.string.transaction_type_recall
        SELF -> R.string.transaction_type_self
        CREATE_STAKE -> R.string.transaction_type_create_stake
        CANCEL_STAKE -> R.string.transaction_type_cancel_stake
        WITHDRAW_STAKE -> R.string.transaction_type_withdraw_stake
        BUY -> R.string.transaction_type_buy
        SELL -> R.string.transaction_type_sell
        UNKNOWN -> R.string.transaction_type_unknown
    }

}
