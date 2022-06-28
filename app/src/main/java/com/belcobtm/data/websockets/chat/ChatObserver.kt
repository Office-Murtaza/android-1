package com.belcobtm.data.websockets.chat

import com.belcobtm.presentation.features.wallet.trade.order.chat.NewMessageItem

interface ChatObserver {

    fun connect()

    fun sendMessage(messageItem: NewMessageItem)

}
