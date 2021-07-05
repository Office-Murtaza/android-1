package com.belcobtm.presentation.features.wallet.trade.create.model

import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.features.wallet.trade.list.model.TradePayment

data class AvailableTradePaymentOption(val payment: TradePayment, val selected: Boolean) : ListItem by payment