package com.app.belcobtm.presentation.features.wallet.trade.main.item

import com.app.belcobtm.domain.wallet.item.TradeDataItem

fun TradeDataItem.mapToUiBuySellItem(isBuyType: Boolean): TradeDetailsItem.BuySell = TradeDetailsItem.BuySell(
    id = id,
    minLimit = minLimit,
    maxLimit = maxLimit,
    tradeCount = tradeCount,
    distance = distance,
    rate = rate,
    userName = userName,
    paymentMethod = paymentMethod,
    price = price,
    terms = terms,
    isBuyType = isBuyType
)

fun TradeDataItem.mapToUiMyItem(): TradeDetailsItem.My = TradeDetailsItem.My(
    id = id,
    userName = userName,
    minLimit = minLimit,
    maxLimit = maxLimit,
    tradeCount = tradeCount,
    distance = distance,
    rate = rate,
    paymentMethod = paymentMethod,
    price = price,
    isBuyType = true
)

fun TradeDataItem.mapToUiOpenItem(): TradeDetailsItem.Open = TradeDetailsItem.Open(
    id = id,
    userName = userName,
    minLimit = minLimit,
    maxLimit = maxLimit,
    tradeCount = tradeCount,
    distance = distance,
    rate = rate,
    paymentMethod = paymentMethod,
    price = price,
    isBuyType = true
)