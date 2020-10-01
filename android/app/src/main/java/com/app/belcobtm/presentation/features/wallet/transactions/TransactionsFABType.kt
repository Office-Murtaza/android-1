package com.app.belcobtm.presentation.features.wallet.transactions

import com.app.belcobtm.R

enum class TransactionsFABType(val id: Int, val resText: Int, val resIcon: Int) {
    STAKING(1001, R.string.transitions_staking, R.drawable.ic_money),
    TRADE(1002, R.string.transitions_trade, R.drawable.ic_trade),
    EXCHANGE(1003, R.string.transitions_exchange, R.drawable.ic_exchange),
    SELL(1004, R.string.sell, R.drawable.ic_shopping_cart),
    SEND_GIFT(1005, R.string.send_gift, R.drawable.ic_gift),
    WITHDRAW(1006, R.string.withdraw, R.drawable.ic_publish),
    DEPOSIT(1007, R.string.deposit, R.drawable.ic_file_download)
}