package com.belcobtm.presentation.features.atm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.belcobtm.R
import com.belcobtm.data.rest.atm.response.OperationType
import com.belcobtm.databinding.AtmInfoWindowBinding
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.extensions.setDrawableEnd
import com.belcobtm.presentation.core.extensions.setDrawableStart
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.formatter.GoogleMapsDirectionQueryFormatter
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.google.android.material.transition.MaterialFade
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class AtmPopupFragment : BaseFragment<AtmInfoWindowBinding>() {

    override val isHomeButtonEnabled: Boolean = false
    override val isToolbarEnabled: Boolean = false

    companion object {

        const val ATM_BUNDLE_KEY = "info.window.popup.atm.key"

        fun newInstance(atm: AtmItem) = AtmPopupFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ATM_BUNDLE_KEY, atm)
            }
        }
    }

    private val googleMapQueryFormatter by inject<Formatter<GoogleMapsDirectionQueryFormatter.Location>>(
        named(GoogleMapsDirectionQueryFormatter.GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER)
    )

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(AtmInfoPopupOpenHoursDelegate())
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        AtmInfoWindowBinding.inflate(inflater, container, false)

    override fun AtmInfoWindowBinding.initViews() {
        val atm = requireArguments().getParcelable<AtmItem>(ATM_BUNDLE_KEY)
            ?: throw IllegalStateException("No atm object in arguments")
        atmName.text = atm.title
        atmAddress.text = atm.address
        atmOpenHours.adapter = adapter
        atmOpenHours.layoutManager = LinearLayoutManager(requireContext())
        if (atm.type == OperationType.BUY_AND_SELL_ONLY) {
            atmType.setText(R.string.atm_type_buy_and_sell)
            atmType.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.atm_type_buy_and_sell_bg
            )
            atmType.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.atm_type_buy_and_sell_text_color
                )
            )
            atmType.setDrawableStart(R.drawable.ic_atm_type_buy_and_sell)
        } else {
            atmType.setText(R.string.atm_type_buy_only)
            atmType.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.atm_type_buy_only_bg
            )
            atmType.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.atm_type_buy_only_text_color
                )
            )
            atmType.setDrawableStart(R.drawable.ic_atm_type_buy_only)
        }
        adapter.update(atm.openHours)
        closeButton.setOnClickListener {
            (parentFragment?.parentFragment as? AtmFragment)?.closePopup()
        }
        atmDistance.text = atm.distance
        if (atm.distance.isNotEmpty()) {
            atmDistance.setDrawableStart(R.drawable.ic_location)
        } else {
            atmDistance.background = null
        }
        getDirection.setOnClickListener {
            val gmmIntentUri = Uri.parse(
                googleMapQueryFormatter.format(
                    GoogleMapsDirectionQueryFormatter.Location(
                        atm.latLng.latitude, atm.latLng.longitude
                    )
                )
            )
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage(requireContext().getString(R.string.google_maps_package))
            startActivity(mapIntent)
        }
        showDetails.setOnClickListener {
            TransitionManager.beginDelayedTransition(root, MaterialFade())
            if (atmOpenHours.visibility == View.VISIBLE) {
                atmOpenHours.visibility = View.GONE
                showDetails.setText(R.string.show_details_button_label)
                showDetails.setDrawableEnd(R.drawable.ic_chevron_down)
            } else {
                atmOpenHours.visibility = View.VISIBLE
                showDetails.setText(R.string.hide_details_button_label)
                showDetails.setDrawableEnd(R.drawable.ic_chevron_up)
            }
        }
    }

}