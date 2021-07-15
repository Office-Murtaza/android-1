package com.belcobtm.presentation.features.atm

import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.belcobtm.R
import com.belcobtm.databinding.ItemAtmInfoPopupOpenHoursBinding
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder

class AtmInfoPopupOpenHoursViewHolder(
    private val binding: ItemAtmInfoPopupOpenHoursBinding
) : MultiTypeViewHolder<OpenHoursItem>(binding.root) {

    override fun bind(model: OpenHoursItem) {
        binding.day.text = model.day
        binding.openHours.text = model.hours
        val (color, typeFace) = if (model.isActive) {
            ContextCompat.getColor(
                binding.root.context, R.color.black_text_color
            ) to Typeface.BOLD
        } else {
            ContextCompat.getColor(
                binding.root.context, R.color.atm_location_label_text_color
            ) to Typeface.NORMAL
        }
        binding.day.setTextColor(color)
        binding.day.setTypeface(null, typeFace)
        binding.openHours.setTypeface(null, typeFace)
        if (model.isClosed) {
            binding.openHours.setText(R.string.atm_closed_hours)
            binding.openHours.setTextColor(
                ContextCompat.getColor(binding.root.context, R.color.colorError)
            )
        } else {
            binding.openHours.setTextColor(color)
        }
    }
}