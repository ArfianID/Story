package com.arfian.story.view.upload

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.arfian.story.R
import com.arfian.story.data.service.responses.Result
import com.arfian.story.databinding.ActivityAddStoryBinding
import com.arfian.story.utils.getImageUri
import com.arfian.story.utils.openFile
import com.arfian.story.utils.reduceFileImage
import com.arfian.story.view.ViewModelFactory
import com.arfian.story.view.story.home.HomeStoryActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private lateinit var locationCallback: LocationCallback
    private var latitude: Float? = null
    private var longitude: Float? = null
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        showLoading()
        setupButtons()
        initializeLocationCallback()
        requestLocationPermission()
    }

    private fun setupButtons() {
        binding.apply {
            galleryButton.setOnClickListener { startGallery() }
            cameraButton.setOnClickListener { startCamera() }
            uploadButton.setOnClickListener { CoroutineScope(Dispatchers.IO).launch { uploadStory() } }
        }
    }

    private fun initializeLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    if (location != null) {
                        latitude = location.latitude.toFloat()
                        longitude = location.longitude.toFloat()
                    }
                }
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Toast.makeText(this@AddStoryActivity,
                getString(R.string.no_media_selected), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private suspend fun uploadStory() {
        val description = binding.descriptionEditText.text.toString()

        try {
            if (description.isEmpty()) {
                showToast(getString(R.string.please_enter_a_description))
                return
            }

            if (currentImageUri == null) {
                showToast(getString(R.string.please_select_an_image))
                return
            }
        } catch (e: Exception) {
            showToast(getString(R.string.error, e.message))
            return
        }

        val inputStream = openFile(this, currentImageUri!!)
        if (inputStream == null) {
            showToast(getString(R.string.failed_to_open_file))
            return
        }

        val compressedInputStream = compressImage(inputStream)

        setLoadingState(true)

        when (val result = viewModel.uploadStory(compressedInputStream, description, latitude, longitude)) {
            is Result.Loading -> {
                viewModel.setLoadingState(true)
            }
            is Result.Success -> {
                withContext(Dispatchers.Main) {
                    viewModel.setLoadingState(false)
                    Toast.makeText(this@AddStoryActivity, result.data, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AddStoryActivity, HomeStoryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
            is Result.Error -> {
                withContext(Dispatchers.Main) {
                    viewModel.setLoadingState(false)
                    AlertDialog.Builder(this@AddStoryActivity)
                        .setTitle(getString(R.string.failed))
                        .setMessage(result.exception.message)
                        .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }
    }

    private suspend fun compressImage(inputStream: InputStream): InputStream {
        val file = withContext(Dispatchers.IO) {
            File.createTempFile("temp", "jpg")
        }
        val outputStream = withContext(Dispatchers.IO) {
            FileOutputStream(file)
        }
        inputStream.copyTo(outputStream)

        val compressedFile = file.reduceFileImage()
        return withContext(Dispatchers.IO) {
            FileInputStream(compressedFile)
        }
    }

    private suspend fun setLoadingState(isLoading: Boolean) {
        withContext(Dispatchers.Main) {
            viewModel.setLoadingState(isLoading)
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@AddStoryActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun showLoading() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Permission already granted, get the location
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get the location
                getLocation()
            } else {
                // Permission not granted, show a message
                Toast.makeText(this, "Location permission not granted. Location will not be posted.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.Builder(10000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private companion object {
        const val REQUEST_LOCATION_PERMISSION = 100
    }
}