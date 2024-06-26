package com.arfian.story.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arfian.story.data.StoryRepository
import com.arfian.story.data.service.responses.Result

class LoginViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    suspend fun login(email: String, password: String): Result<String> {
        return repository.login(email, password)
    }
}