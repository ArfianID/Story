package com.arfian.story.view.story.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arfian.story.data.StoryRepository
import com.arfian.story.data.pref.SessionModel
import com.arfian.story.data.room.StoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeStoryViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun getSession(): Flow<SessionModel> {
        return repository.getSession()
    }

    fun getStories(): Flow<PagingData<StoryEntity>> {
        return repository.getStories().cachedIn(viewModelScope)
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