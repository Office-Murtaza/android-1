package com.app.belcobtm.presentation.features.atm

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.app.belcobtm.R
import com.app.belcobtm.data.rest.atm.response.AtmResponse
import com.app.belcobtm.databinding.AtmInfoWindowBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker


class AtmInfoWindowAdapter(val context: Context) : GoogleMap.InfoWindowAdapter {

    private val binding: AtmInfoWindowBinding =
        AtmInfoWindowBinding.inflate(LayoutInflater.from(context), null, false)

    private fun renderWindowText(marker: Marker) {

        val atmAddress = marker.tag as AtmResponse.AtmAddress

        binding.atmName.text = atmAddress.name
        binding.atmAddress.text = atmAddress.address

        binding.atmOpenHoursContainer.removeAllViews()
        atmAddress.hours.forEach { openHour ->
            val openHourText = getOpenHoursColorText(openHour.days, openHour.hours)
            val textView = AppCompatTextView(context)
            textView.text = openHourText
            textView.textSize = 12f
            binding.atmOpenHoursContainer.addView(textView)
        }
    }

    private fun getOpenHoursColorText(text1: String, text2: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val text1Spannable = SpannableString("$text1: ")
        text1Spannable.setSpan(
            ForegroundColorSpan(context.getColor(R.color.light_gray_text_color)),
            0,
            text1Spannable.length,
            0
        )
        val text2Spannable = SpannableString(text2)
        text2Spannable.setSpan(
            ForegroundColorSpan(context.getColor(R.color.blue_color)),
            0,
            text2Spannable.length,
            0
        )

        builder.append(text1Spannable).append(text2Spannable)
        return builder
    }

    override fun getInfoWindow(marker: Marker): View {
        renderWindowText(marker)
        return binding.root
    }

    override fun getInfoContents(marker: Marker): View {
        renderWindowText(marker)
        return binding.root
    }
}