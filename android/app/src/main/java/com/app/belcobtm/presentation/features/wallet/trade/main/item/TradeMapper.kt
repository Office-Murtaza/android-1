package com.app.belcobtm.presentation.features.wallet.trade.main.item

import com.app.belcobtm.domain.transaction.item.TradeDataItem

fun TradeDataItem.mapToUiBuySellItem(isBuyType: Boolean): TradeDetailsBuySellItem = TradeDetailsBuySellItem(
    tradeId = id,
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

fun TradeDataItem.mapToUiMyItem(): TradeDetailsMyItem = TradeDetailsMyItem(
    tradeId = id,
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

fun TradeDataItem.mapToUiOpenItem(): TradeDetailsOpenItem = TradeDetailsOpenItem(
    tradeId = id,
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