package com.app.belcobtm.ui.main.atm

import android.Manifest
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.belcobtm.mvp.BaseMvpFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.tbruyelle.rxpermissions2.RxPermissions


class AtmFragment : BaseMvpFragment<AtmContract.View, AtmContract.Presenter>(),
    AtmContract.View
    , GoogleMap.OnInfoWindowClickListener
    , OnMapReadyCallback
    , LocationListener {

    private var mMap: GoogleMap? = null
    private var locationManager: LocationManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.app.belcobtm.R.layout.fragment_atm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager
            .findFragmentById(com.app.belcobtm.R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        mPresenter.requestAtmAddressList()
    }

    override fun onInfoWindowClick(marker: Marker?) {
        Toast.makeText(
            context, "Info window clicked",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        initMarkers()
        mMap?.setInfoWindowAdapter(AtmInfoWindowAdapter(context))
        mMap?.setOnInfoWindowClickListener(this)

        RxPermissions(this).request(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).subscribe { granted ->
            if (granted) {
                mMap?.isMyLocationEnabled = true
                locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, this)
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)

                val criteria = Criteria()
                val bestProvider = locationManager?.getBestProvider(criteria, false)
                val lastKnownLocation = locationManager?.getLastKnownLocation(bestProvider)
                if (lastKnownLocation != null) {
                    onLocationChanged(lastKnownLocation)
                }
            }
        }
    }


    //    LocationListener start
    override fun onLocationChanged(location: Location) {
        val posLat = location.latitude
        val posLng = location.longitude

        val position = LatLng(posLat, posLng)
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 5f))

        locationManager?.removeUpdates(this)

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String?) {}

    override fun onProviderDisabled(provider: String?) {}
//    LocationListener end

    private fun initMarkers() {
        if (mMap != null) {
            mPresenter.atmAddressList.forEach { atmItem ->
                val latLng = LatLng(atmItem.latitude, atmItem.longitude)
                val marker = mMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(atmItem.locationName)
                )
                marker?.tag = atmItem
            }
        }
    }

    override fun notifyAtmAddressList() {
        initMarkers()
    }
}