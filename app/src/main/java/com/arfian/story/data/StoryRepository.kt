package com.arfian.story.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.arfian.story.R
import com.arfian.story.data.pref.LanguagePreference
import com.arfian.story.data.pref.SessionModel
import com.arfian.story.data.pref.SessionPreference
import com.arfian.story.data.service.api.ApiService
import com.arfian.story.data.service.responses.LoginResponse
import com.arfian.story.data.service.responses.RegisterResponse
import com.arfian.story.data.service.responses.Result
import com.arfian.story.data.service.responses.StoryItem
import com.arfian.story.data.service.responses.StoryResponse
import com.arfian.story.data.service.responses.UploadResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoryRepository private constructor(
    private val context: Context,
    private val sessionPreference: SessionPreference,
    private val languagePreference: LanguagePreference,
    private val apiService: ApiService
) {

    private suspend fun saveSession(user: SessionModel) {
        sessionPreference.saveSession(user)
    }

    fun getSession(): Flow<SessionModel> {
        return sessionPreference.getSession()
    }

    suspend fun logout() {
        sessionPreference.logout()
    }

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(name, email, password).execute()
                val body = response.body()
                if (response.isSuccessful && body != null && !body.error) {
                    Result.Success(
                        context.getString(
                            R.string.congratulations_you_re_successfully_registered,
                            name
                        ))
                } else {
                    val message = body?.message ?: response.errorBody()?.string() ?: context.getString(R.string.unknown_error)
                    Result.Error(Exception(message))
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, RegisterResponse::class.java)
                val errorMessage = errorBody.message
                Result.Error(Exception(errorMessage))
            } catch (t: Throwable) {
                Result.Error(t.message?.let { Exception(it) } ?: Exception(context.getString(R.string.unknown_error)))
            }
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(email, password).execute()
                val body = response.body()
                if (response.isSuccessful && body != null && !body.error) {
                    val user = SessionModel(body.loginResult.userId, body.loginResult.token, true)
                    saveSession(user)
                    Result.Success(
                        context.getString(
                            R.string.congratulations_you_re_successfully_logged_in,
                            body.loginResult.name
                        ))
                } else {
                    val message = body?.message ?: response.errorBody()?.string() ?: context.getString(R.string.unknown_error)
                    Result.Error(Exception(message))
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)
                val errorMessage = errorBody.message
                Result.Error(Exception(errorMessage))
            } catch (t: Throwable) {
                Result.Error(t.message?.let { Exception(it) } ?: Exception(context.getString(R.string.unknown_error)))
            }
        }
    }

//    suspend fun getStories(): Result<List<StoryItem>> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = apiService.getStories()
//                Result.Success(response.listStory)
//            } catch (e: HttpException) {
//                val jsonInString = e.response()?.errorBody()?.string()
//                val errorBody = Gson().fromJson(jsonInString, StoryResponse::class.java)
//                val errorMessage = errorBody.message
//                Result.Error(Exception(errorMessage))
//            } catch (t: Throwable) {
//                Result.Error(t.message?.let { Exception(it) } ?: Exception(context.getString(R.string.unknown_error)))
//            }
//        }
//    }

    fun getStories(): Flow<PagingData<StoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService)
            }
        ).flow
    }

    suspend fun getStoriesWithLocation(): Result<List<StoryItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStoriesWithLocation()
                Result.Success(response.listStory)
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, StoryResponse::class.java)
                val errorMessage = errorBody.message
                Result.Error(Exception(errorMessage))
            } catch (t: Throwable) {
                Result.Error(t.message?.let { Exception(it) } ?: Exception(context.getString(R.string.unknown_error)))
            }
        }
    }

    suspend fun addStory(
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.addStory(description, photo, lat, lon).execute()
                val body = response.body()
                if (response.isSuccessful && body != null && !body.error) {
                    Result.Success(context.getString(R.string.story_uploaded_successfully))
                } else {
                    val message = body?.message ?: response.errorBody()?.string() ?: context.getString(R.string.unknown_error)
                    Result.Error(Exception(message))
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, UploadResponse::class.java)
                val errorMessage = errorBody.message
                Result.Error(Exception(errorMessage))
            } catch (t: Throwable) {
                Result.Error(t.message?.let { Exception(it) } ?: Exception(context.getString(R.string.unknown_error)))
            }
        }
    }

    fun getSelectedLanguage(): String {
        return languagePreference.selectedLanguage
    }

    fun setSelectedLanguage(langCode: String) {
        languagePreference.selectedLanguage = langCode
    }

    companion object {
        fun getInstance(
            context: Context,
            sessionPreference: SessionPreference,
            languagePreference: LanguagePreference,
            apiService: ApiService
        ) = StoryRepository(context, sessionPreference, languagePreference, apiService)
    }
}