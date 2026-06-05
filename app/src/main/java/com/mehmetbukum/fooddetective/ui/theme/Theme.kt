package com.mehmetbukum.fooddetective.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalAppDarkTheme = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = CraneDarkGreen,
    onPrimary = CraneInk,
    primaryContainer = CraneDeepGreen,
    onPrimaryContainer = Color(0xFFE8FFF7),
    secondary = CraneDarkGold,
    onSecondary = CraneInk,
    secondaryContainer = Color(0xFF4A3514),
    onSecondaryContainer = Color(0xFFFFE7AF),
    tertiary = CraneDarkGold,
    background = Color(0xFF0D1412),
    onBackground = Color(0xFFE9F2EC),
    surface = CraneDarkCard,
    onSurface = Color(0xFFE9F2EC),
    surfaceVariant = Color(0xFF24332E),
    onSurfaceVariant = Color(0xFFC9D8D1),
    // outline koyu yüzeyde sınırdaydı; biraz açıldı (daha okunur ayraçlar)
    outline = Color(0xFF98A8A1),
    errorContainer = Color(0xFF5A1E1E),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = CraneGreen,
    onPrimary = Color.White,
    primaryContainer = CraneMist,
    onPrimaryContainer = CraneDeepGreen,
    secondary = CraneGold,
    onSecondary = CraneInk,
    secondaryContainer = Color(0xFFFFE7B8),
    onSecondaryContainer = Color(0xFF4F3500),
    tertiary = CraneClay,
    background = CraneIvory,
    onBackground = CraneInk,
    surface = Color(0xFFFFFEFA),
    onSurface = CraneInk,
    surfaceVariant = CraneCream,
    onSurfaceVariant = Color(0xFF4D5A55),
    outline = Color(0xFF8C9A94),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun EDetectiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You; markanı korumak istersen çağrıda dynamicColor = false geç.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Sistem çubuklarını temaya bağla: light temada koyu ikon, dark temada açık ikon.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
