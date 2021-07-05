package com.belcobtm.presentation.features.wallet.trade.order.chat.model

import com.belcobtm.presentation.core.adapter.model.ListItem

data class ChatMessageItem(
    val text: String,
    val time: String,
    val timestamp: Long,
    val imageUrl: String? = null,
    val isFromHistory: Boolean,
    override val type: Int
) : ListItem {

    companion object {
        const val MY_MESSAGE_TYPE = 1
        const val PARTNER_MESSAGE_TYPE = 2
    }

    override val id: String
        get() = time + type
}