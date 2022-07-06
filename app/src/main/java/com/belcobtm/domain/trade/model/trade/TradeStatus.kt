package com.belcobtm.domain.trade.model.trade

enum class TradeStatus {

    ACTIVE,
    CANCELED,
    DELETED,
    UNKNOWN // exists only on client to handle null

}
