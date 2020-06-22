package com.app.belcobtm.domain.transaction.type

enum class TransactionStatusType(val code: Int) {
    UNKNOWN(0),
    PENDING(1),
    COMPLETE(2),
    FAIL(3)
}