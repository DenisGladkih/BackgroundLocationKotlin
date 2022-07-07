package com.gladkih.backgroundlocationkotlin.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gladkih.backgroundlocationkotlin.R
import com.gladkih.backgroundlocationkotlin.databinding.ActivityMainBinding
import com.gladkih.backgroundlocationkotlin.other.Constants.ACTION_START_SERVICE
import com.gladkih.backgroundlocationkotlin.other.Constants.ACTION_STOP_SERVICE
import com.gladkih.backgroundlocationkotlin.other.Constants.MAP_ZOOM
import com.gladkih.backgroundlocationkotlin.other.Constants.POLYLINE_COLOR
import com.gladkih.backgroundlocationkotlin.other.Constants.POLYLINE_WIDTH
import com.gladkih.backgroundlocationkotlin.other.Constants.RC_BACKGROUND_LOCATION_PERMISSIONS
import com.gladkih.backgroundlocationkotlin.other.Constants.RC_LOCATION_PERMISSIONS
import com.gladkih.backgroundlocationkotlin.other.Constants.SERVICE_TAG
import com.gladkih.backgroundlocationkotlin.repositories.MainRepository
import com.gladkih.backgroundlocationkotlin.services.TrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, OnMapReadyCallback {

    @Inject
    lateinit var repository: MainRepository

    private lateinit var binding: ActivityMainBinding
    private var map: GoogleMap? = null
    private var serviceIsRunning = false
    private var polyLineList = mutableListOf<LatLng>()

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        TrackingService.isTracking.observe(this) {
            serviceIsRunning = it
            if (serviceIsRunning) binding.btnStart.text = getString(R.string.stop)
            else binding.btnStart.text = getString(R.string.start)
        }

        binding.btnStart.setOnClickListener {
            if (!serviceIsRunning) {
                if (hasLocationPermissions()) {
                    sendCommandToService(ACTION_START_SERVICE)
                    map?.isMyLocationEnabled = true
                    polyLineList = mutableListOf()
                    map?.clear()
                } else requestLocationPermissions()
            }

            if (serviceIsRunning) {
                sendCommandToService(ACTION_STOP_SERVICE)
                map?.isMyLocationEnabled = false

            }
        }
    }


    private fun sendCommandToService(action: String) =
        Intent(this, TrackingService::class.java).also {
            it.action = action
            this.startService(it)
        }


    private fun hasLocationPermissions() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }


    private fun requestLocationPermissions() {
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.permissions_rationale),
            RC_LOCATION_PERMISSIONS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this, getString(R.string.permissions_rationale),
                RC_BACKGROUND_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }


    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestLocationPermissions()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(this, getString(R.string.permissions_granted), Toast.LENGTH_SHORT).show()
    }


    private fun drawAllLines() {
        repository.getAllUserLocations().observe(this) { userLocationList ->
            Log.d(SERVICE_TAG, "userLocationList.size:  ${userLocationList.size}")
            polyLineList = mutableListOf()

            if (userLocationList.isNotEmpty()) {
                for (userLocation in userLocationList) {
                    polyLineList.add(userLocation.getLatLng())
                }
            }

            if (polyLineList.isNotEmpty()) {
                val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)

                polylineOptions.points.addAll(polyLineList)
                map?.addPolyline(polylineOptions)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(polyLineList.last(), MAP_ZOOM))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.setMinZoomPreference(MAP_ZOOM)
        drawAllLines()
    }
}
