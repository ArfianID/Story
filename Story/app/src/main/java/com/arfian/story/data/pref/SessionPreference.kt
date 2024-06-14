package com.arfian.story.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.arfian.story.data.pref.PreferenceKeys.EMAIL_KEY
import com.arfian.story.data.pref.PreferenceKeys.IS_LOGIN_KEY
import com.arfian.story.data.pref.PreferenceKeys.TOKEN_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSession(user: SessionModel) {
        dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = user.email
            preferences[TOKEN_KEY] = user.token
            preferences[IS_LOGIN_KEY] = true
        }
    }

    fun getSession(): Flow<SessionModel> {
        return dataStore.data.map { preferences ->
            val session = SessionModel(
                preferences[EMAIL_KEY] ?: "",
                preferences[TOKEN_KEY] ?: "",
                preferences[IS_LOGIN_KEY] ?: false
            )
            session
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "session_pref"

        private var INSTANCE: SessionPreference? = null

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

        fun getInstance(context: Context): SessionPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionPreference(context.dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}