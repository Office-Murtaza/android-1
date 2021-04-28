package com.app.belcobtm.presentation.features.wallet.trade.order.chat.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemPartnerMessageBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

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
        Glide.with(binding.root)
            .load(model.imageUrl)
            .apply(RequestOptions().override(binding.attachment.width, binding.attachment.height))
            .into(binding.attachment)
    }
}