package com.arfian.story.view.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arfian.story.data.StoryRepository
import com.arfian.story.data.service.responses.Result

class RegisterViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return repository.register(name, email, password)
    }
}