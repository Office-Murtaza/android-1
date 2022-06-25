package com.belcobtm.domain.transaction.type

enum class TransactionCashStatusType {
    PENDING,
    AVAILABLE,
    WITHDRAWN,
    UNKNOWN // exists only on client to handle null
}
