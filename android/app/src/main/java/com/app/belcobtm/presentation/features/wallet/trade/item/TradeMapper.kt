package com.app.belcobtm.presentation.features.wallet.trade.item

import com.app.belcobtm.domain.wallet.item.TradeDataItem
import com.app.belcobtm.presentation.core.extensions.toStringUsd

fun TradeDataItem.mapToUiBuyItem(): TradeListItem.Buy = TradeListItem.Buy(
    id = id,
    userName = userName,
    paymentMethod = paymentMethod,
    price = price.toStringUsd(),
    priceLimit = "$minLimit - $maxLimit"
)