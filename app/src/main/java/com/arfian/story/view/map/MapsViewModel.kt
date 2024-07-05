package com.arfian.story.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arfian.story.data.StoryRepository
import com.arfian.story.data.service.responses.Result
import com.arfian.story.data.service.responses.StoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun setLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun getStoriesWithLocation(): Flow<Result<List<StoryItem>>> {
        return flow {
            emit(Result.Loading)
            val result = storyRepository.getStoriesWithLocation()
            emit(result)
        }
    }

}