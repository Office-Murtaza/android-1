package com.belcobtm.presentation.features.atm

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.belcobtm.R
import com.belcobtm.data.rest.atm.response.OperationType
import com.belcobtm.databinding.AtmInfoBottomSheetBinding
import com.belcobtm.databinding.FragmentAtmBinding
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.extensions.setDrawableEnd
import com.belcobtm.presentation.core.extensions.setDrawableStart
import com.belcobtm.presentation.core.extensions.toggle
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.formatter.GoogleMapsDirectionQueryFormatter
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialFade
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.RuntimePermissions


@RuntimePermissions
class AtmFragment : BaseFragment<FragmentAtmBinding>(),
    GoogleMap.OnInfoWindowClickListener,
    OnMapReadyCallback {
    private val viewModel by viewModel<AtmViewModel>()

    override var isMenuEnabled = true
    override val isToolbarEnabled = false
    override val retryListener: View.OnClickListener =
        View.OnClickListener { viewModel.requestAtms() }

    private var map: GoogleMap? = null
    private var location: Location? = null
    private var appliedState: LoadingData<AtmsInfoItem>? = null
    private var locationAvailable: Boolean = false

    private val bottomSheetBehavior: BottomSheetBehavior<View> by lazy {
        BottomSheetBehavior.from(binding.bottomSheet.root)
    }

    private val googleMapQueryFormatter by inject<Formatter<GoogleMapsDirectionQueryFormatter.Location>>(
        named(GoogleMapsDirectionQueryFormatter.GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER)
    )

    private val adapter by lazy {
        MultiTypeAdapter().apply {
            registerDelegate(AtmInfoPopupOpenHoursDelegate())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onInfoWindowClick(marker: Marker?) {
    }

    override fun FragmentAtmBinding.initObservers() {
        viewModel.stateData.listen(
            success = { state ->
                state.doIfChanged(appliedState?.commonData) {
                    if (map != null) {
                        it.location?.let(::onLocationChanged)
                    }
                    initMarkers(it.atms)
                }
            },
            onUpdate = {
                appliedState = it
            }
        )
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        map.setOnMarkerClickListener { marker: Marker ->
            binding.bottomSheet.update(marker.tag as AtmItem)
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            true
        }
        onLocationPermissionGrantedWithPermissionCheck()
        viewModel.requestAtms()
    }

    private fun AtmInfoBottomSheetBinding.update(atm: AtmItem) {
        TransitionManager.beginDelayedTransition(root, Fade())
        getDirection.toggle(locationAvailable)
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
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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

    @SuppressLint("MissingPermission")
    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun onLocationPermissionGranted() {
        map?.isMyLocationEnabled = true
        locationAvailable = true
        location?.let(::onLocationChanged)
    }

    @OnNeverAskAgain(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun permissionsNeverAskAgain() {
        AlertHelper.showToastShort(requireContext(), R.string.verification_please_on_permissions)
    }

    private fun onLocationChanged(location: Location) {
        val posLat = location.latitude
        val posLng = location.longitude
        val position = LatLng(posLat, posLng)
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 5f))
    }

    private fun initMarkers(atms: List<AtmItem>) {
        if (map != null) {
            atms.forEach { atmItem ->
                val marker = map?.addMarker(
                    MarkerOptions()
                        .position(LatLng(atmItem.latLng.latitude, atmItem.latLng.longitude))
                        .icon(
                            bitmapDescriptorFromVector(
                                requireContext(),
                                R.drawable.ic_atm_marker
                            )
                        )
                        .title(atmItem.title)
                )
                marker?.tag = atmItem
            }
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAtmBinding =
        FragmentAtmBinding.inflate(inflater, container, false)

    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorDrawableResourceId: Int
    ): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}