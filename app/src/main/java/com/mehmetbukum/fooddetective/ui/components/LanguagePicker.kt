package com.mehmetbukum.fooddetective.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehmetbukum.fooddetective.R
import com.mehmetbukum.fooddetective.localization.AppLanguage

private fun AppLanguage.shortLabel(): String = when (this) {
    AppLanguage.SYSTEM -> "SYS"
    AppLanguage.TURKISH -> "TR"
    AppLanguage.ENGLISH -> "EN"
}

@Composable
fun LanguagePicker(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.a11y_language_picker)
    var expanded by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = selectedLanguage.shortLabel(),
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
            LanguageMenuItem(
                label = stringResource(R.string.language_system),
                short = AppLanguage.SYSTEM.shortLabel(),
                selected = selectedLanguage == AppLanguage.SYSTEM,
                onClick = {
                    onLanguageSelected(AppLanguage.SYSTEM)
                    expanded = false
                }
            )
            LanguageMenuItem(
                label = stringResource(R.string.language_turkish),
                short = AppLanguage.TURKISH.shortLabel(),
                selected = selectedLanguage == AppLanguage.TURKISH,
                onClick = {
                    onLanguageSelected(AppLanguage.TURKISH)
                    expanded = false
                }
            )
            LanguageMenuItem(
                label = stringResource(R.string.language_english),
                short = AppLanguage.ENGLISH.shortLabel(),
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
private fun LanguageMenuItem(
    label: String,
    short: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = short,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    text = label,
                    fontWeight = if (selected) FontWeight.Black else FontWeight.Normal
                )
            }
        },
        onClick = onClick
    )
}
