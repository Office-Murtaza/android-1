package com.belcobtm.presentation.screens.services.adapter

import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.belcobtm.R
import com.belcobtm.databinding.ItemServiceBinding
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder

class ServiceItemViewHolder(
    private val binding: ItemServiceBinding,
    onServiceClicked: (ServiceItem) -> Unit,
    onVerifyClicked: () -> Unit,
) : MultiTypeViewHolder<ServiceItem>(binding.root) {

    init {
        binding.root.setOnClickListener {
            onServiceClicked(model)
        }
        binding.verificationError.setOnClickListener { onVerifyClicked() }
    }

    override fun bind(model: ServiceItem) {

        binding.imageView.setImageDrawable(
            ContextCompat.getDrawable(
                binding.root.context,
                model.icon
            )
        )
        binding.labelText.text = binding.root.context.getString(model.title)

        if (!model.locationEnabled || !model.verificationEnabled) {
            binding.root.setOnClickListener { }
            binding.labelText.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.light_gray
                )
            )
            binding.imageView.setColorFilter(
                ContextCompat.getColor(binding.root.context, R.color.light_gray),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            binding.ivChevron.setColorFilter(
                ContextCompat.getColor(binding.root.context, R.color.light_gray),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

            binding.locationError.isVisible = model.locationEnabled.not()
            binding.locationError.isInvisible = model.verificationEnabled
            binding.verificationError.isVisible = model.verificationEnabled.not()
        }

    }

}
