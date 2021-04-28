package com.app.belcobtm.presentation.features.wallet.trade.order.chat.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemMyMessageBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.model.ChatMessageItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MyMessageDelegate : AdapterDelegate<ChatMessageItem, MyMessageViewHolder>() {

    override val viewType: Int
        get() = ChatMessageItem.MY_MESSAGE_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): MyMessageViewHolder =
        MyMessageViewHolder(ItemMyMessageBinding.inflate(inflater, parent, false))
}

class MyMessageViewHolder(
    private val binding: ItemMyMessageBinding
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