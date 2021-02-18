package com.udacity.nanodegree.locationreminder.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.nanodegree.locationreminder.R
import com.udacity.nanodegree.locationreminder.base.BaseFragment
import com.udacity.nanodegree.locationreminder.base.NavigationCommand
import com.udacity.nanodegree.locationreminder.databinding.FragmentSelectLocationBinding
import com.udacity.nanodegree.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.nanodegree.locationreminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        val TAG: String = SelectLocationFragment::class.java.simpleName
        const val DEFAULT_ZOOM = 17f
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 2
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 3
    }

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private var googleMap: GoogleMap? = null
    private val runningOnQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    var reminderSelectedLocationStr = ""
    lateinit var selectedPOI: PointOfInterest
    var latitude = 0.0
    var longitude = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        setupGoogleMap()

        binding.selectLocationFragmentSaveLocation.setOnClickListener {
            if (reminderSelectedLocationStr.isNotBlank()) {
                onLocationSelected()
            } else
                _viewModel.showToast.value = "You must select location"
        }


        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.reminderSelectedLocationStr.value = reminderSelectedLocationStr
        _viewModel.selectedPOI.value = selectedPOI
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude
        _viewModel.navigationCommand.value = NavigationCommand.Back

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setupGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.select_location_fragment_map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap

        askForegroundBackgroundLocationPermission()
        zoomToUserLocation()
        setMapStyle()
        setMapLongClickListener()
        setOnPoiClickListener()
        setOnMyLocationClickListener()

    }

    private fun setOnMyLocationClickListener() {
        googleMap?.setOnMyLocationButtonClickListener {
            val location = googleMap?.myLocation ?: return@setOnMyLocationButtonClickListener false
            val latLng = LatLng(location.latitude, location.longitude)
            updateCurrentLocation(latLng)
            return@setOnMyLocationButtonClickListener true
        }

        googleMap?.setOnMyLocationClickListener { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            updateCurrentLocation(latLng)
        }
    }

    private fun updateCurrentLocation(latLng: LatLng) {
        reminderSelectedLocationStr = latLng.toString()
        selectedPOI = PointOfInterest(latLng, reminderSelectedLocationStr, "Current Location")
        latitude = latLng.latitude
        longitude = latLng.longitude
    }

    @SuppressLint("MissingPermission")
    fun zoomToUserLocation() {
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(requireActivity()) {
            it?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                val zoomLevel = DEFAULT_ZOOM
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        zoomLevel
                    )
                )
            }
        }
    }


    private fun addMarker(latLng: LatLng, zoomLevel: Float = DEFAULT_ZOOM) {
        val snippet = String.format(
            Locale.getDefault(),
            "Lat: %1$.5f, Long: %2$.5f",
            latLng.latitude,
            latLng.longitude
        )

        this.googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        this.googleMap?.addMarker(
            MarkerOptions().position(latLng)
                .title(getString(R.string.dropped_pin))
                .snippet(snippet)
        )
        reminderSelectedLocationStr = latLng.toString()
        selectedPOI = PointOfInterest(latLng, reminderSelectedLocationStr, "Custom Location")
        latitude = latLng.latitude
        longitude = latLng.longitude

    }

    private fun setMapStyle() {
        try {
            googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.google_map_style
                )
            )
        } catch (e: Resources.NotFoundException) {

        }
    }


    private fun setMapLongClickListener() {
        this.googleMap?.setOnMapLongClickListener { latLng ->
            addMarker(latLng)
        }
    }

    private fun setOnPoiClickListener() {
        googleMap?.setOnPoiClickListener { poi ->
            googleMap?.clear()
            val poiMarker = googleMap?.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            reminderSelectedLocationStr = poi.name
            selectedPOI = poi
            latitude = poi.latLng.latitude
            longitude = poi.latLng.longitude
        }
    }

    private fun askForegroundBackgroundLocationPermission() {
        if (foregroundAndBackgroundLocationPermissionApproved()) checkLocationAndStartGeoFence()
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = if (runningOnQOrLater) {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        } else REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ))
        val backgroundLocationApproved =
            if (runningOnQOrLater)
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) else true
        return foregroundLocationApproved && backgroundLocationApproved
    }


    @SuppressLint("MissingPermission")
    private fun checkLocationAndStartGeoFence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val settingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        settingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                googleMap?.isMyLocationEnabled = true
                zoomToUserLocation()
            }
        }.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) try {
                startIntentSenderForResult(
                    exception.resolution.intentSender,
                    REQUEST_TURN_DEVICE_LOCATION_ON,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } catch (sendEx: IntentSender.SendIntentException) {

            } else
                Snackbar.make(
                    binding.rootLayout,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkLocationAndStartGeoFence()
                }.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED
            || (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
        else checkLocationAndStartGeoFence()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkLocationAndStartGeoFence(false)
        }
    }

}
