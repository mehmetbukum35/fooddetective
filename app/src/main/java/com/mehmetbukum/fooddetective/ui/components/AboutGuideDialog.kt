package com.mehmetbukum.fooddetective.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mehmetbukum.fooddetective.R
import com.mehmetbukum.fooddetective.localization.AppLanguage
import com.mehmetbukum.fooddetective.localization.withAppLanguage

@Composable
fun AboutGuideDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val baseContext = LocalContext.current
    val localizedContext = remember(baseContext, language) {
        baseContext.withAppLanguage(language)
    }
    val appVersion = remember(baseContext) { baseContext.readAppVersionName() }

    fun text(resId: Int): String = localizedContext.getString(resId)
    fun text(resId: Int, vararg args: Any): String = localizedContext.getString(resId, *args)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = text(R.string.about_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                GuideSection(
                    title = text(R.string.about_section_what_title),
                    body = text(R.string.about_section_what_body)
                )
                GuideSection(
                    title = text(R.string.about_section_how_title),
                    body = text(R.string.about_section_how_body)
                )
                GuideSection(
                    title = text(R.string.about_section_status_title),
                    body = text(R.string.about_section_status_body)
                )
                GuideSection(
                    title = text(R.string.about_section_source_title),
                    body = text(R.string.about_section_source_body)
                )
                GuideSection(
                    title = text(R.string.about_section_privacy_title),
                    body = text(R.string.about_section_privacy_body)
                )
                ContactAndPrivacySection(
                    feedbackTitle = text(R.string.feedback_button),
                    feedbackEmail = text(R.string.feedback_email),
                    privacyTitle = text(R.string.privacy_policy_button),
                    privacyUrl = text(R.string.privacy_policy_url),
                    emailCopiedMessage = text(R.string.toast_email_copied),
                    privacyCopiedMessage = text(R.string.toast_privacy_url_copied),
                    context = baseContext
                )
                GuideSection(
                    title = text(R.string.about_section_app_info_title),
                    body = text(R.string.about_section_app_info_body, appVersion)
                )
                GuideSection(
                    title = text(R.string.about_section_warning_title),
                    body = text(R.string.about_section_warning_body)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text(R.string.about_dialog_close))
            }
        }
    )
}

@Composable
private fun GuideSection(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContactAndPrivacySection(
    feedbackTitle: String,
    feedbackEmail: String,
    privacyTitle: String,
    privacyUrl: String,
    emailCopiedMessage: String,
    privacyCopiedMessage: String,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
    ) {
        Text(
            text = feedbackTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        TextButton(
            onClick = {
                context.copyTextToClipboard(label = feedbackEmail, text = feedbackEmail)
                Toast.makeText(context, emailCopiedMessage, Toast.LENGTH_LONG).show()
            }
        ) {
            Text(feedbackEmail)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = privacyTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        TextButton(
            onClick = {
                context.copyTextToClipboard(label = privacyUrl, text = privacyUrl)
                Toast.makeText(context, privacyCopiedMessage, Toast.LENGTH_LONG).show()
            }
        ) {
            Text(privacyUrl)
        }
    }
}

private fun Context.readAppVersionName(): String {
    return runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
    }.getOrDefault("1.0")
}

private fun Context.copyTextToClipboard(label: String, text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
