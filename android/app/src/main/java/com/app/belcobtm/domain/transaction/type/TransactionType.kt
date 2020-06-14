package com.app.belcobtm.domain.transaction.type

enum class TransactionType(val code: Int) {
    UNKNOWN(0),
    DEPOSIT(1),
    WITHDRAW(2),
    SEND_GIFT(3),
    RECEIVE_GIFT(4),
    BUY(5),
    SELL(6),
    SEND_COIN_TO_COIN(8),
    RECEIVE_COIN_TO_COIN(9)
}