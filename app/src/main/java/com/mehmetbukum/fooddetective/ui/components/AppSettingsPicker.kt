package com.mehmetbukum.fooddetective.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehmetbukum.fooddetective.ApiConnectionStatus
import com.mehmetbukum.fooddetective.R
import com.mehmetbukum.fooddetective.UiText
import com.mehmetbukum.fooddetective.asString
import com.mehmetbukum.fooddetective.localization.AppLanguage
import com.mehmetbukum.fooddetective.ui.theme.AppThemeMode

@Composable
fun AppSettingsPicker(
    selectedTheme: AppThemeMode,
    selectedLanguage: AppLanguage,
    onThemeSelected: (AppThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    apiConnectionStatus: ApiConnectionStatus = ApiConnectionStatus.CHECKING,
    syncMessage: UiText? = null,
    onAboutClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.a11y_settings_picker)

    Box(modifier = modifier.semantics { contentDescription = description }) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.22f),
            contentColor = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MenuHeader(text = stringResource(R.string.sync_info_title))
            DatabaseStatusSection(
                status = apiConnectionStatus,
                syncMessage = syncMessage
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

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

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            MenuHeader(text = stringResource(R.string.about_section_what_title))
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.about_dialog_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                onClick = {
                    expanded = false
                    onAboutClick()
                }
            )
        }
    }
}

@Composable
private fun DatabaseStatusSection(
    status: ApiConnectionStatus,
    syncMessage: UiText?
) {
    val label = apiStatusLabel(status)
    val dotColor = apiStatusColor(status)

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .width(280.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        syncMessage?.let { message ->
            Text(
                text = message.asString(),
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun apiStatusLabel(status: ApiConnectionStatus): String {
    return when (status) {
        ApiConnectionStatus.CHECKING -> stringResource(R.string.api_status_checking)
        ApiConnectionStatus.ONLINE -> stringResource(R.string.api_status_online)
        ApiConnectionStatus.LOCAL -> stringResource(R.string.api_status_local)
        ApiConnectionStatus.OFFLINE -> stringResource(R.string.api_status_offline)
    }
}

private fun apiStatusColor(status: ApiConnectionStatus): Color {
    return when (status) {
        ApiConnectionStatus.CHECKING -> Color(0xFFFFC107)
        ApiConnectionStatus.ONLINE -> Color(0xFF2ECC71)
        ApiConnectionStatus.LOCAL -> Color(0xFF42A5F5)
        ApiConnectionStatus.OFFLINE -> Color(0xFFE74C3C)
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
