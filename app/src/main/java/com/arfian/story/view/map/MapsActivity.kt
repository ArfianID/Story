package com.arfian.story.view.map

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.arfian.story.R
import com.arfian.story.data.service.responses.Result
import com.arfian.story.data.service.responses.StoryItem
import com.arfian.story.databinding.ActivityMapsBinding
import com.arfian.story.view.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val viewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        lifecycleScope.launch {
            viewModel.setLoadingState(true)
            viewModel.getStoriesWithLocation().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        viewModel.setLoadingState(true)
                    }

                    is Result.Success -> {
                        viewModel.setLoadingState(false)
                        val stories = result.data
                        showsManyMarker(stories)
                    }


                    is Result.Error -> {
                        viewModel.setLoadingState(false)
                        AlertDialog.Builder(this@MapsActivity)
                            .setTitle(getString(R.string.failed))
                            .setMessage(result.exception.message)
                            .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
            }
        }

        getMyLocation()

    }

    private fun showsManyMarker(data: List<StoryItem>) {
        data.forEach {
            val latLng = LatLng(it.lat, it.lon)
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(it.name)
                    .snippet(it.description)
            )

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }
    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}