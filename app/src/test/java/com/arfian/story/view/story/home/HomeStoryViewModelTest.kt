package com.arfian.story.view.story.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.arfian.story.DataDummy
import com.arfian.story.MainDispatcherRule
import com.arfian.story.data.StoryRepository
import com.arfian.story.data.service.api.ApiService
import com.arfian.story.data.service.responses.StoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class HomeStoryViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Test
    fun `when Get Stories Returns Data then Data Is Returned`() = runTest {
        val dummyStoryItems = DataDummy.generateDummyQuoteResponse()
        val data: PagingData<StoryItem> = StoryPagingSource.snapshot(dummyStoryItems)
        val expectedStories = MutableStateFlow(data)

        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedStories)
        val homeStoryViewModel = HomeStoryViewModel(storyRepository)
        val actualStories: PagingData<StoryItem> = homeStoryViewModel.getStories().first()

        val differ = AsyncPagingDataDiffer(
            diffCallback = HomeStoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)

        Assert.assertNotNull("Expected snapshot to be not null", differ.snapshot())
        Assert.assertEquals("Expected snapshot size to be equal to dummy data size", dummyStoryItems.size, differ.snapshot().size)
        Assert.assertEquals("Expected first item in snapshot to be equal to first item in dummy data", dummyStoryItems[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Stories Returns Empty Then No Data Is Returned`() = runTest {
        val emptyData: PagingData<StoryItem> = PagingData.from(emptyList())
        val expectedStories = MutableStateFlow(emptyData)

        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedStories)
        val homeStoryViewModel = HomeStoryViewModel(storyRepository)
        val actualStories: PagingData<StoryItem> = homeStoryViewModel.getStories().first()

        val differ = AsyncPagingDataDiffer(
            diffCallback = HomeStoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)

        Assert.assertEquals("Expected 0 items in the snapshot", 0, differ.snapshot().size)
    }
}

class StoryPagingSource(private val apiService: ApiService) : PagingSource<Int, StoryItem>() {
    companion object {
        fun snapshot(items: List<StoryItem>): PagingData<StoryItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryItem> {
        val initialPageIndex = 1
        val position = params.key ?: initialPageIndex
        return try {
            val responseData = apiService.getStories(position, params.loadSize)
            LoadResult.Page(
                data = responseData.listStory,
                prevKey = if (position == initialPageIndex) null else position - 1,
                nextKey = if (responseData.listStory.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}

private val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}

class HomeStoryAdapter {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryItem>() {
            override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}