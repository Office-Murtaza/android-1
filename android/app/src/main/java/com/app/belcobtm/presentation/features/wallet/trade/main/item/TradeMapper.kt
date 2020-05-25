package com.app.belcobtm.presentation.features.wallet.trade.main.item

import com.app.belcobtm.domain.wallet.item.TradeDataItem

fun TradeDataItem.mapToUiBuyItem(): TradeDetailsItem.Buy = TradeDetailsItem.Buy(
    id = id,
    minLimit = minLimit,
    maxLimit = maxLimit,
    tradeCount = tradeCount,
    distance = distance,
    rate = rate,
    userName = userName,
    paymentMethod = paymentMethod,
    price = price,
    terms = terms
)

fun TradeDataItem.mapToUiSellItem(): TradeDetailsItem.Sell = TradeDetailsItem.Sell(
    id = id,
    minLimit = minLimit,
    maxLimit = maxLimit,
    tradeCount = tradeCount,
    distance = distance,
    rate = rate,
    userName = userName,
    paymentMethod = paymentMethod,
    price = price
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