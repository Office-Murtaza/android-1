package com.belcobtm.presentation.features.deals.delegate

import androidx.core.content.ContextCompat
import com.belcobtm.R
import com.belcobtm.databinding.ItemDealsBinding
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.tools.extensions.invisible
import com.belcobtm.presentation.tools.extensions.show

class DealsItemViewHolder(
    private val binding: ItemDealsBinding,
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

            if (!model.locationEnabled) {
                binding.locationError.show()
            }
            if (!model.verificationEnabled) {
                binding.locationError.invisible()
                binding.verificationError.show()
            }
        }


    }

}
