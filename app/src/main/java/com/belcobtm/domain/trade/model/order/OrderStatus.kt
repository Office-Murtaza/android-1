package com.belcobtm.domain.trade.model.order

enum class OrderStatus {

    NEW,
    CANCELED,
    DOING,
    PAID,
    RELEASED,
    DISPUTING,
    SOLVED,
    DELETED,
    UNKNOWN // exists only on client to handle null

}
