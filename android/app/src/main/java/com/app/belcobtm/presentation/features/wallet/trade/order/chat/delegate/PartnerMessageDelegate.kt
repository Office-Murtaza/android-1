package com.app.belcobtm.presentation.features.wallet.trade.order.chat.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemPartnerMessageBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem

class PartnerMessageDelegate : AdapterDelegate<ChatMessageItem, PartnerMessageViewHolder>() {

    override val viewType: Int
        get() = ChatMessageItem.PARTNER_MESSAGE_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): PartnerMessageViewHolder =
        PartnerMessageViewHolder(ItemPartnerMessageBinding.inflate(inflater, parent, false))
}

class PartnerMessageViewHolder(
    private val binding: ItemPartnerMessageBinding
) : MultiTypeViewHolder<ChatMessageItem>(binding.root) {

    override fun bind(model: ChatMessageItem) {
        binding.message.text = model.text
        binding.time.text = model.time
        binding.attachment.setImageBitmap(model.bitmap)
    }
}