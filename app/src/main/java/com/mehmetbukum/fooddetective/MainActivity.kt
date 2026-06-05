package com.mehmetbukum.fooddetective

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mehmetbukum.fooddetective.data.AdditiveRepository
import com.mehmetbukum.fooddetective.data.AdditivesVersionResponse
import com.mehmetbukum.fooddetective.data.AppDatabase
import com.mehmetbukum.fooddetective.data.remote.AdditivesRemoteFactory
import com.mehmetbukum.fooddetective.localization.AppLanguagePreferences
import com.mehmetbukum.fooddetective.localization.AppLocaleProvider
import com.mehmetbukum.fooddetective.ui.screens.FoodDetectiveScreen
import com.mehmetbukum.fooddetective.ui.theme.AppThemePreferences
import com.mehmetbukum.fooddetective.ui.theme.EDetectiveTheme
import com.mehmetbukum.fooddetective.ui.theme.isDarkTheme
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = AdditiveRepository(
            dao = database.additiveDao(),
            remoteDataSource = AdditivesRemoteFactory.create(isDebuggableApp())
        )
        val detectiveViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FoodDetectiveViewModel(repository) as T
                }
            }
        )[FoodDetectiveViewModel::class.java]

        runScheduledApiSync(detectiveViewModel)

        setContent {
            var selectedLanguage by remember {
                mutableStateOf(AppLanguagePreferences.getLanguage(this))
            }
            var selectedTheme by remember {
                mutableStateOf(AppThemePreferences.getTheme(this))
            }
            val darkTheme = selectedTheme.isDarkTheme()

            AppLocaleProvider(language = selectedLanguage) {
                EDetectiveTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        FoodDetectiveScreen(
                            viewModel = detectiveViewModel,
                            selectedLanguage = selectedLanguage,
                            selectedTheme = selectedTheme,
                            onLanguageSelected = { language ->
                                AppLanguagePreferences.setLanguage(this, language)
                                selectedLanguage = language
                            },
                            onThemeSelected = { theme ->
                                AppThemePreferences.setTheme(this, theme)
                                selectedTheme = theme
                            }
                        )
                    }
                }
            }
        }
    }

    private fun runScheduledApiSync(viewModel: FoodDetectiveViewModel) {
        viewModel.runScheduledApiSync(
            shouldCheckSync = shouldCheckSync(),
            lastSuccessfulVersionHash = syncPreferences.getString(KEY_LAST_VERSION_HASH, null),
            lastSuccessfulSyncText = getLastSuccessfulSyncText(),
            onSuccessfulSync = ::markSyncSuccessful
        )
    }

    private fun shouldCheckSync(): Boolean {
        val lastSyncMillis = syncPreferences.getLong(KEY_LAST_SUCCESSFUL_SYNC_MS, 0L)
        if (lastSyncMillis <= 0L) return true

        val elapsedMillis = System.currentTimeMillis() - lastSyncMillis
        return elapsedMillis >= SYNC_INTERVAL_MS
    }

    private fun markSyncSuccessful(version: AdditivesVersionResponse): String {
        val nowMillis = System.currentTimeMillis()
        syncPreferences.edit()
            .putLong(KEY_LAST_SUCCESSFUL_SYNC_MS, nowMillis)
            .putString(KEY_LAST_VERSION_HASH, version.version_hash.orEmpty())
            .putInt(KEY_LAST_TOTAL_COUNT, version.total_count)
            .apply()

        return formatSyncTime(nowMillis)
    }

    private fun getLastSuccessfulSyncText(): String? {
        val lastSyncMillis = syncPreferences.getLong(KEY_LAST_SUCCESSFUL_SYNC_MS, 0L)
        return lastSyncMillis
            .takeIf { it > 0L }
            ?.let(::formatSyncTime)
    }

    private fun formatSyncTime(timeMillis: Long): String {
        val date = Date(timeMillis)
        val dateText = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)
        val timeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(date)
        return "$dateText $timeText"
    }

    private fun isDebuggableApp(): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private val syncPreferences by lazy {
        getSharedPreferences(SYNC_PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val SYNC_PREFS_NAME = "additives_sync_prefs"
        private const val KEY_LAST_SUCCESSFUL_SYNC_MS = "last_successful_sync_ms"
        private const val KEY_LAST_VERSION_HASH = "last_version_hash"
        private const val KEY_LAST_TOTAL_COUNT = "last_total_count"
        private const val SYNC_INTERVAL_MS = 6 * 60 * 60 * 1000L
    }
}
