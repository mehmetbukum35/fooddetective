package com.mehmetbukum.fooddetective.localization

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

enum class AppLanguage(val storageKey: String, val localeTag: String?) {
    SYSTEM("system", null),
    TURKISH("tr", "tr"),
    ENGLISH("en", "en");

    companion object {
        fun fromStorageKey(key: String?): AppLanguage {
            return entries.firstOrNull { it.storageKey == key } ?: SYSTEM
        }
    }
}

object AppLanguagePreferences {
    private const val PREFS_NAME = "language_preferences"
    private const val KEY_APP_LANGUAGE = "app_language"

    fun getLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppLanguage.fromStorageKey(prefs.getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.storageKey))
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_LANGUAGE, language.storageKey)
            .apply()
    }
}

@Composable
fun AppLocaleProvider(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
    val localizedContext = remember(baseContext, language) {
        baseContext.withAppLanguage(language)
    }
    val localizedConfiguration = localizedContext.resources.configuration

    if (activityResultRegistryOwner != null) {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedConfiguration,
            LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
            content = content
        )
    } else {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedConfiguration,
            content = content
        )
    }
}

fun Context.withAppLanguage(language: AppLanguage): Context {
    val localeTag = language.localeTag ?: return this
    val locale = Locale.forLanguageTag(localeTag)
    Locale.setDefault(locale)

    val configuration = Configuration(resources.configuration)
    configuration.setLocales(LocaleList(locale))
    configuration.setLayoutDirection(locale)

    return createConfigurationContext(configuration)
}

fun isCurrentAppLanguageEnglish(context: Context): Boolean {
    return context.resources.configuration.locales[0].language == "en"
}
