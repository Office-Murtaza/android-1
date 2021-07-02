package com.app.belcobtm.presentation.features.wallet.trade.create.model

import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradePayment

data class AvailableTradePaymentOption(val payment: TradePayment, val selected: Boolean) : ListItem by payment