package com.app.belcobtm.presentation.features.wallet.transactions

import com.app.belcobtm.R

enum class TransactionsFABType(val id: Int, val resText: Int, val resIcon: Int) {
    STAKING(1001, R.string.transitions_staking, R.drawable.ic_money),
    TRADE(1002, R.string.transitions_trade, R.drawable.ic_trade),
    EXCHANGE(1003, R.string.transitions_exchange, R.drawable.ic_exchange),
    SELL(1004, R.string.sell, R.drawable.ic_shopping_cart),
    RECALL(1006, R.string.recall, R.drawable.ic_recall),
    RESERVE(1007, R.string.reserve, R.drawable.ic_reserve),
    WITHDRAW(1008, R.string.withdraw, R.drawable.ic_vector_up),
    DEPOSIT(1009, R.string.deposit, R.drawable.ic_deposit)
}