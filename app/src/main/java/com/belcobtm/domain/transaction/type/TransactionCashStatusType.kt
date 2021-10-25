package com.belcobtm.domain.transaction.type

enum class TransactionCashStatusType(val code: Int) {
    UNKNOWN(0),
    PENDING(1),
    AVAILABLE(2),
    WITHDRAWN(3)
}