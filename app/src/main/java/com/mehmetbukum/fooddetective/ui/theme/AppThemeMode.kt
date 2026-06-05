package com.mehmetbukum.fooddetective.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

enum class AppThemeMode(val storageKey: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorageKey(key: String?): AppThemeMode {
            return entries.firstOrNull { it.storageKey == key } ?: SYSTEM
        }
    }
}

object AppThemePreferences {
    private const val PREFS_NAME = "theme_preferences"
    private const val KEY_APP_THEME = "app_theme"

    fun getTheme(context: Context): AppThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppThemeMode.fromStorageKey(prefs.getString(KEY_APP_THEME, AppThemeMode.SYSTEM.storageKey))
    }

    fun setTheme(context: Context, theme: AppThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_THEME, theme.storageKey)
            .apply()
    }
}

@Composable
fun AppThemeMode.isDarkTheme(): Boolean {
    return when (this) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
}
