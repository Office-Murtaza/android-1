package com.app.belcobtm.presentation.features.wallet.trade.order.chat.model

import android.graphics.Bitmap
import com.app.belcobtm.presentation.core.adapter.model.ListItem

data class ChatMessageItem(
    val text: String,
    val time: String,
    val bitmap: Bitmap? = null,
    override val type: Int
) : ListItem {

    companion object {
        const val MY_MESSAGE_TYPE = 1
        const val PARTNER_MESSAGE_TYPE = 2
    }

    override val id: String
        get() = time + MY_MESSAGE_TYPE
}