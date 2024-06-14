package com.arfian.story.data.pref

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val EMAIL_KEY = stringPreferencesKey("email")
    val TOKEN_KEY = stringPreferencesKey("token")
    val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")
    const val LANGUAGE_KEY = "language_key"
}