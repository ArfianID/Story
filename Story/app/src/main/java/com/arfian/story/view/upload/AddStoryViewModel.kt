package com.arfian.story.view.upload

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arfian.story.data.StoryRepository
import com.arfian.story.data.service.responses.Result
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class AddStoryViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private var isUploading = false

    fun setLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    suspend fun uploadStory(
        inputStream: InputStream,
        description: String,
        lat: Float?,
        lon: Float?
    ): Result<String> {
        isUploading = true
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

        inputStream.use {
            val bytes = it.readBytes()
            val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", "image.jpg", requestFile)

            val latPart = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val lonPart = lon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val result = repository.addStory(descriptionPart, photoPart, latPart, lonPart)
            isUploading = false
            return result
        }
    }
}