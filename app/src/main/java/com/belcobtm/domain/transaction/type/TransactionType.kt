package com.belcobtm.domain.transaction.type

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
    UNKNOWN // exists only on client to handle null
}
