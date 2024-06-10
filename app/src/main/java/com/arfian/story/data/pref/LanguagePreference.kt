package com.arfian.story.data.pref

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

class LanguagePreference(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFERENCES_NAME = "language_pref"
    }

    var selectedLanguage: String
        get() = preferences.getString(PreferenceKeys.LANGUAGE_KEY, Locale.getDefault().language)
            ?: Locale.getDefault().language
        set(value) = preferences.edit().putString(PreferenceKeys.LANGUAGE_KEY, value).apply()
}