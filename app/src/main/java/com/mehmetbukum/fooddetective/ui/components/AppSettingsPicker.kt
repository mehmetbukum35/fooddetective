package com.mehmetbukum.fooddetective.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehmetbukum.fooddetective.R
import com.mehmetbukum.fooddetective.localization.AppLanguage
import com.mehmetbukum.fooddetective.ui.theme.AppThemeMode

@Composable
fun AppSettingsPicker(
    selectedTheme: AppThemeMode,
    selectedLanguage: AppLanguage,
    onThemeSelected: (AppThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.a11y_settings_picker)

    Box(modifier = modifier.semantics { contentDescription = description }) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(100.dp),
            color = Color.White.copy(alpha = 0.22f),
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.settings_short_label),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MenuHeader(text = stringResource(R.string.settings_theme_title))
            ThemeItem(
                label = stringResource(R.string.theme_system),
                icon = Icons.Default.PhoneAndroid,
                selected = selectedTheme == AppThemeMode.SYSTEM,
                onClick = {
                    onThemeSelected(AppThemeMode.SYSTEM)
                    expanded = false
                }
            )
            ThemeItem(
                label = stringResource(R.string.theme_light),
                icon = Icons.Default.WbSunny,
                selected = selectedTheme == AppThemeMode.LIGHT,
                onClick = {
                    onThemeSelected(AppThemeMode.LIGHT)
                    expanded = false
                }
            )
            ThemeItem(
                label = stringResource(R.string.theme_dark),
                icon = Icons.Default.DarkMode,
                selected = selectedTheme == AppThemeMode.DARK,
                onClick = {
                    onThemeSelected(AppThemeMode.DARK)
                    expanded = false
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            MenuHeader(text = stringResource(R.string.settings_language_title))
            LanguageItem(
                label = stringResource(R.string.language_system),
                short = "SYS",
                selected = selectedLanguage == AppLanguage.SYSTEM,
                onClick = {
                    onLanguageSelected(AppLanguage.SYSTEM)
                    expanded = false
                }
            )
            LanguageItem(
                label = stringResource(R.string.language_turkish),
                short = "TR",
                selected = selectedLanguage == AppLanguage.TURKISH,
                onClick = {
                    onLanguageSelected(AppLanguage.TURKISH)
                    expanded = false
                }
            )
            LanguageItem(
                label = stringResource(R.string.language_english),
                short = "EN",
                selected = selectedLanguage == AppLanguage.ENGLISH,
                onClick = {
                    onLanguageSelected(AppLanguage.ENGLISH)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun MenuHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontSize = 11.sp,
        letterSpacing = 1.8.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ThemeItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        },
        text = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Normal
            )
        },
        trailingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        onClick = onClick
    )
}

@Composable
private fun LanguageItem(
    label: String,
    short: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        leadingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = short,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(34.dp)
                )
            }
        },
        text = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Normal
            )
        },
        trailingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        onClick = onClick
    )
}
