package com.arfian.story.di

import android.content.Context
import com.arfian.story.data.StoryRepository
import com.arfian.story.data.pref.LanguagePreference
import com.arfian.story.data.pref.SessionPreference
import com.arfian.story.data.service.api.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {

    fun provideRepository(context: Context): StoryRepository {
        val pref = SessionPreference.getInstance(context)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        val languagePreference = LanguagePreference(context)
        return StoryRepository.getInstance(context, pref, languagePreference, apiService)
    }
}