package com.arfian.story.view.upload

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.arfian.story.R
import com.arfian.story.databinding.ActivityAddStoryBinding
import com.arfian.story.utils.getImageUri
import com.arfian.story.utils.openFile
import com.arfian.story.utils.reduceFileImage
import com.arfian.story.view.ViewModelFactory
import com.arfian.story.view.story.list.ListStoryActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import com.arfian.story.data.service.responses.Result

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
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
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { CoroutineScope(Dispatchers.IO).launch { uploadStory() } }
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
        val lat: Float? = null
        val lon: Float? = null

        try {
            if (description.isEmpty()) {
                CoroutineScope(Dispatchers.Main).launch { showToast(getString(R.string.please_enter_a_description)) }
                return
            }

            if (currentImageUri == null) {
                CoroutineScope(Dispatchers.Main).launch { showToast(getString(R.string.please_select_an_image)) }
                return
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch { showToast(getString(R.string.error, e.message)) }
        }

        val inputStream = openFile(this, currentImageUri!!)
        if (inputStream == null) {
            CoroutineScope(Dispatchers.Main).launch { showToast(getString(R.string.failed_to_open_file)) }
            return
        }

        val compressedInputStream = compressImage(inputStream)

        withContext(Dispatchers.Main) { setLoadingState(true) }

        when (val result = viewModel.uploadStory(compressedInputStream, description, lat, lon)) {
            is Result.Loading -> {
                viewModel.setLoadingState(true)
            }
            is Result.Success -> {
                viewModel.setLoadingState(false)
                Toast.makeText(this@AddStoryActivity, result.data, Toast.LENGTH_SHORT).show()
                val intent = Intent(this@AddStoryActivity, ListStoryActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            is Result.Error -> {
                viewModel.setLoadingState(false)
                AlertDialog.Builder(this@AddStoryActivity)
                    .setTitle(getString(R.string.failed))
                    .setMessage(result.exception.message)
                    .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                    .show()
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
}