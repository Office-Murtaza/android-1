package com.app.belcobtm.domain.transaction.type

enum class TransactionCashStatusType(val code: Int) {
    UNKNOWN(0),
    NOT_AVAILABLE(1),
    AVAILABLE(2),
    WITHDRAWN(3)
}