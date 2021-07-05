package com.belcobtm.presentation.features.atm

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.data.rest.atm.response.AtmResponse
import com.belcobtm.databinding.FragmentAtmBinding
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
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
    override val retryListener: View.OnClickListener = View.OnClickListener { viewModel.requestAtms() }

    private var map: GoogleMap? = null
    private var locationManager: LocationManager? = null
    private var appliedState: LoadingData<List<AtmResponse.AtmAddress>>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
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
        this.map?.setInfoWindowAdapter(AtmInfoWindowAdapter(requireContext()))
        this.map?.setOnInfoWindowClickListener(this)

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
        val bestProvider = locationManager?.getBestProvider(criteria, false)
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

    override fun onProviderEnabled(provider: String?) {}

    override fun onProviderDisabled(provider: String?) {}

    private fun initMarkers(atms: List<AtmResponse.AtmAddress>) {
        if (map != null) {
            atms.forEach { atmItem ->
                val marker = map?.addMarker(
                    MarkerOptions()
                        .position(LatLng(atmItem.latitude, atmItem.longitude))
                        .title(atmItem.name)
                )
                marker?.tag = atmItem
            }
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAtmBinding =
        FragmentAtmBinding.inflate(inflater, container, false)
}