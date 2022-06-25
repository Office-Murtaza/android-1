package com.belcobtm.domain.transaction.type

enum class TransactionStatusType {
    COMPLETE,
    PENDING,
    FAIL,
    NOT_EXIST,
    UNKNOWN // exists only on client to handle null
}
