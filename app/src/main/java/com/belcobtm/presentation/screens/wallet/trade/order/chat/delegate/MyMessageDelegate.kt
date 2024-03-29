package com.belcobtm.presentation.screens.wallet.trade.order.chat.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.belcobtm.databinding.ItemMyMessageBinding
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.screens.wallet.trade.order.chat.model.ChatMessageItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MyMessageDelegate(
    private val onImageClicked: (String) -> Unit
) : AdapterDelegate<ChatMessageItem, MyMessageViewHolder>() {

    override val viewType: Int
        get() = ChatMessageItem.MY_MESSAGE_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): MyMessageViewHolder =
        MyMessageViewHolder(
            ItemMyMessageBinding.inflate(inflater, parent, false),
            onImageClicked
        )
}

class MyMessageViewHolder(
    private val binding: ItemMyMessageBinding,
    private val onImageClicked: (String) -> Unit
) : MultiTypeViewHolder<ChatMessageItem>(binding.root) {

    init {
        binding.attachment.setOnClickListener {
            onImageClicked(model.imageUrl.orEmpty())
        }
    }

    override fun bind(model: ChatMessageItem) {
        binding.message.apply {
            isVisible = model.text.isNotEmpty()
            text = model.text
        }
        binding.time.text = model.time

        if (!model.imageUrl.isNullOrEmpty()) {
            binding.attachment.isVisible = true
            Glide.with(binding.root)
                .load(model.imageUrl)
                .apply(RequestOptions().override(binding.attachment.width, binding.attachment.height))
                .into(binding.attachment)
        } else {
            binding.attachment.isVisible = false
        }
    }

}
