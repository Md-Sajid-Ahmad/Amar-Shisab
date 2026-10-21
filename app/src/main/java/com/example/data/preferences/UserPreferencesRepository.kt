package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "amar_hisab_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("pref_color_theme")
        val KEY_LANGUAGE = stringPreferencesKey("pref_app_language")

        const val THEME_BLUE = "BLUE"
        const val THEME_LIME = "LIME"
        const val THEME_EMERALD = "EMERALD"
        const val THEME_AMETHYST = "AMETHYST"

        const val LANG_BN = "BN"
        const val LANG_EN = "EN"
    }

    val themeKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: THEME_BLUE
    }

    val languageKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: LANG_BN
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }
}
