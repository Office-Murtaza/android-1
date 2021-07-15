package com.belcobtm.presentation.features.atm

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment
import com.belcobtm.R
import com.belcobtm.databinding.FragmentAtmBinding
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import org.koin.android.viewmodel.ext.android.viewModel
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.RuntimePermissions


@RuntimePermissions
class AtmFragment : BaseFragment<FragmentAtmBinding>(),
    GoogleMap.OnInfoWindowClickListener,
    OnMapReadyCallback,
    LocationListener {
    private val viewModel by viewModel<AtmViewModel>()

    override var isMenuEnabled = true
    override val isToolbarEnabled = false
    override val retryListener: View.OnClickListener =
        View.OnClickListener { viewModel.requestAtms() }

    private var map: GoogleMap? = null
    private var infoWindow: InfoWindow? = null
    private var locationManager: LocationManager? = null
    private var appliedState: LoadingData<List<AtmItem>>? = null
    private val mapInfoWindowFragment by lazy {
        childFragmentManager.findFragmentById(R.id.map) as MapInfoWindowFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as MapInfoWindowFragment
        mapFragment.getMapAsync(this)
    }

    override fun onInfoWindowClick(marker: Marker?) {
//        AlertHelper.showToastShort(requireContext(),  "Info window clicked")
    }

    override fun FragmentAtmBinding.initObservers() {
        viewModel.stateData.listen(
            success = { state ->
                state.doIfChanged(appliedState?.commonData) {
                    initMarkers(it)
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
            val spec: InfoWindow.MarkerSpecification =
                InfoWindow.MarkerSpecification(0, 0)
            val infoWindow = InfoWindow(
                marker, spec, AtmPopupFragment.newInstance(marker.tag as AtmItem)
            ).also(::infoWindow::set)
            mapInfoWindowFragment.infoWindowManager().toggle(infoWindow, true)
            true
        }
        onLocationPermissionGrantedWithPermissionCheck()
        viewModel.requestAtms()
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun onLocationPermissionGranted() {
        this.map?.isMyLocationEnabled = true
        locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000,
            10f,
            this
        )
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0,
            0f,
            this
        )

        val criteria = Criteria()
        val bestProvider = locationManager?.getBestProvider(criteria, false) ?: return
        val lastKnownLocation = locationManager?.getLastKnownLocation(bestProvider)
        if (lastKnownLocation != null) {
            onLocationChanged(lastKnownLocation)
        }
    }


    @OnNeverAskAgain(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    fun permissionsNeverAskAgain() {
        AlertHelper.showToastShort(requireContext(), R.string.verification_please_on_permissions)
    }

    //    LocationListener start
    override fun onLocationChanged(location: Location) {
        val posLat = location.latitude
        val posLng = location.longitude

        val position = LatLng(posLat, posLng)
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 5f))

        locationManager?.removeUpdates(this)

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

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

    fun closePopup() {
        infoWindow?.let {
            mapInfoWindowFragment.infoWindowManager().hide(it, true)
        }
    }

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