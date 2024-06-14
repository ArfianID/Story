package com.arfian.story.view.story.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arfian.story.data.StoryRepository
import com.arfian.story.data.pref.SessionModel
import com.arfian.story.data.service.responses.StoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import com.arfian.story.data.service.responses.Result

class ListStoryViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun getSession(): Flow<SessionModel> {
        return repository.getSession()
    }

    fun getStories(): Flow<Result<List<StoryItem>>> {
        return flow {
            emit(Result.Loading)
            val result = repository.getStories()
            emit(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun getSelectedLanguage(): String {
        return repository.getSelectedLanguage()
    }

    fun setSelectedLanguage(langCode: String) {
        repository.setSelectedLanguage(langCode)
    }
}