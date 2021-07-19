package com.belcobtm.presentation.features.referral.contacts.holder

import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.belcobtm.R
import com.belcobtm.databinding.ItemInviteContactBinding
import com.belcobtm.domain.contacts.item.Contact
import com.belcobtm.domain.referral.item.SelectableContact
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.features.contacts.adapter.ContactListDiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

class InviteContactsViewHolder(
    private val binding: ItemInviteContactBinding,
    contactClickListener: (SelectableContact) -> Unit
) : MultiTypeViewHolder<SelectableContact>(binding.root) {

    init {
        binding.root.setOnClickListener {
            contactClickListener(model)
        }
    }

    override fun bind(model: SelectableContact) {
        with(binding) {
            pickContact.isChecked = model.isSelected
            updateTextViews(model.contact)
            Glide.with(binding.root)
                .run {
                    if (model.contact.photoUri.isNotEmpty()) {
                        load(Uri.parse(model.contact.photoUri))
                    } else {
                        load(R.drawable.ic_default_contact)
                    }
                }
                .transform(CircleCrop())
                .apply(RequestOptions().override(contactImage.width, contactImage.height))
                .into(contactImage)
        }
    }

    override fun bindPayload(model: SelectableContact, payloads: List<Any>) {
        if (payloads.contains(ContactListDiffUtil.CONTACT_HIGHLIGHTED_PART_CHANGED)) {
            binding.pickContact.isChecked = model.isSelected
            binding.updateTextViews(model.contact)
        } else {
            super.bindPayload(model, payloads)
        }
    }

    private fun ItemInviteContactBinding.updateTextViews(model: Contact) {
        contactPhone.text = SpannableStringBuilder(model.phoneNumber).apply {
            model.phoneNumberHighlightRange?.let { range ->
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    range.first,
                    range.last,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }
        contactName.text = SpannableStringBuilder(model.displayName).apply {
            model.displayNameHighlightRange?.let { range ->
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    range.first,
                    range.last,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }
    }
}