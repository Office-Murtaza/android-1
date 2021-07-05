package com.belcobtm.domain.transaction.type

enum class TransactionType(val code: Int) {
    UNKNOWN(0),
    DEPOSIT(1),
    WITHDRAW(2),
    SEND_TRANSFER(3),
    RECEIVE_TRANSFER(4),
    BUY(5),
    SELL(6),
    MOVE(7),
    SWAP_SEND(8),
    SWAP_RECEIVE(9),
    RESERVE(10),
    RECALL(11),
    SELF(12),
    STAKE_CREATE(13),
    STAKE_CANCEL(14),
    STAKE_WITHDRAW(15)
}