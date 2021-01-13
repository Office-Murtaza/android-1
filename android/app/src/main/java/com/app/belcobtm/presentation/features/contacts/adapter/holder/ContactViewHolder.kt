package com.app.belcobtm.presentation.features.contacts.adapter.holder

import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.app.belcobtm.R
import com.app.belcobtm.databinding.ItemContactBinding
import com.app.belcobtm.domain.contacts.item.Contact
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.contacts.adapter.ContactListDiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

class ContactViewHolder(
    private val binding: ItemContactBinding,
    contactClickListener: (Contact) -> Unit
) : MultiTypeViewHolder<Contact>(binding.root) {

    init {
        binding.root.setOnClickListener {
            contactClickListener(model)
        }
    }

    override fun bind(model: Contact) {
        with(binding) {
            updateTextViews(model)
            Glide.with(binding.root)
                .run {
                    if (model.photoUri.isNotEmpty()) {
                        load(Uri.parse(model.photoUri))
                    } else {
                        load(R.drawable.ic_default_contact)
                    }
                }
                .transform(CircleCrop())
                .apply(RequestOptions().override(contactImage.width, contactImage.height))
                .into(contactImage)
        }
    }

    override fun bindPayload(model: Contact, payloads: List<Any>) {
        if (payloads.contains(ContactListDiffUtil.CONTACT_HIGHLIGHTED_PART_CHANGED)) {
            binding.updateTextViews(model)
        } else {
            super.bindPayload(model, payloads)
        }
    }

    private fun ItemContactBinding.updateTextViews(model: Contact) {
        contactPhone.text = SpannableStringBuilder(model.phoneNumber).apply {
            model.phoneNumberHighlightRange?.let { range ->
                setSpan(StyleSpan(Typeface.BOLD), range.first, range.last, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        contactName.text = SpannableStringBuilder(model.displayName).apply {
            model.displayNameHighlightRange?.let { range ->
                setSpan(StyleSpan(Typeface.BOLD), range.first, range.last, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
    }
}