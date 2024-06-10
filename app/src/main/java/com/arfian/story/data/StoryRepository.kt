package com.arfian.story.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.arfian.story.data.pref.LanguagePreference
import com.arfian.story.data.pref.SessionModel
import com.arfian.story.data.pref.SessionPreference
import com.arfian.story.data.service.api.ApiService
import com.arfian.story.data.service.responses.LoginResponse
import com.arfian.story.data.service.responses.RegisterResponse
import com.arfian.story.data.service.responses.StoryItem
import com.arfian.story.data.service.responses.StoryResponse
import com.arfian.story.data.service.responses.UploadResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoryRepository private constructor(
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
        Log.d("StoryRepository", "logout: Logging out user")
        sessionPreference.logout()
        Log.d("StoryRepository", "logout: User logged out successfully")
    }

    suspend fun register(name: String, email: String, password: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(name, email, password).execute()
                val body = response.body()
                if (response.isSuccessful && body != null && !body.error) {
                    Pair(false, "Congratulations $name, you're successfully registered!")
                } else {
                    val message = body?.message ?: response.errorBody()?.string() ?: "Unknown error"
                    Pair(false, message)
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, RegisterResponse::class.java)
                val errorMessage = errorBody.message
                Pair(false, errorMessage)
            } catch (t: Throwable) {
                Pair(false, t.message ?: "Unknown error")
            }
        }
    }

    suspend fun login(email: String, password: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(email, password).execute()
                val body = response.body()
                if (response.isSuccessful && body != null && !body.error) {
                    val user = SessionModel(body.loginResult.userId, body.loginResult.token, true)
                    saveSession(user)
                    Log.d("StoryRepository", "saveSession login: email: $email, token: ${body.loginResult.token}")
                    Pair(
                        true,
                        "Congratulations ${body.loginResult.name}, you're successfully logged in"
                    )
                } else {
                    val message = body?.message ?: response.errorBody()?.string() ?: "Unknown error"
                    Pair(false, message)
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)
                val errorMessage = errorBody.message
                Pair(false, errorMessage)
            } catch (t: Throwable) {
                Pair(false, t.message ?: "Unknown error")
            }
        }
    }

    suspend fun getStories(): List<StoryItem> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStories()
                Log.d(
                    "StoryRepository",
                    "getStories: response received, list size = ${response.listStory.size}"
                )
                response.listStory
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, StoryResponse::class.java)
                val errorMessage = errorBody.message
                Log.e("StoryRepository", "HttpException in getStories: $errorMessage")
                emptyList()
            } catch (t: Throwable) {
                Log.e("StoryRepository", "Throwable in getStories: ${t.message}")
                emptyList()
            }
        }
    }

    suspend fun addStory(
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.addStory(description, photo, lat, lon).execute()
                val body = response.body()
                if (response.isSuccessful && body != null && !body.error) {
                    Pair(true, "Story uploaded successfully")
                } else {
                    val message = body?.message ?: response.errorBody()?.string() ?: "Unknown error"
                    Pair(false, message)
                }
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, UploadResponse::class.java)
                val errorMessage = errorBody.message
                Pair(false, errorMessage)
            } catch (t: Throwable) {
                Pair(false, t.message ?: "Unknown error")
            }
        }
    }

    fun getSelectedLanguage(): String {
        return languagePreference.selectedLanguage
    }

    fun setSelectedLanguage(langCode: String) {
        languagePreference.selectedLanguage = langCode
    }

//    var token: String = runBlocking { sessionPreference.getSession().first().token }
//
//    companion object {
//        @Volatile
//        private var INSTANCE: StoryRepository? = null
//
//        fun getInstance(
//            sessionPreference: SessionPreference,
//            languagePreference: LanguagePreference,
//            apiService: ApiService
//        ): StoryRepository {
//            INSTANCE = null
//            return INSTANCE ?: synchronized(this) {
//                val instance = StoryRepository(sessionPreference, languagePreference, apiService)
//                instance.token = runBlocking { sessionPreference.getSession().first().token }
//                INSTANCE = instance
//                instance
//            }
//        }
//    }

//    companion object {
//        @Volatile
//        private var instance: StoryRepository? = null
//        fun getInstance(
//            sessionPreference: SessionPreference,
//            languagePreference: LanguagePreference,
//            apiService: ApiService
//        ): StoryRepository =
//            instance ?: synchronized(this) {
//                instance ?: StoryRepository(sessionPreference, languagePreference, apiService)
//            }.also { instance = it }
//    }

    companion object {
        fun getInstance(
            sessionPreference: SessionPreference,
            languagePreference: LanguagePreference,
            apiService: ApiService
        ) = StoryRepository(sessionPreference, languagePreference, apiService)
    }
}