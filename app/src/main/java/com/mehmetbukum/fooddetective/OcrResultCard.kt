package com.mehmetbukum.fooddetective

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehmetbukum.fooddetective.data.OcrSearchResult

@Composable
fun OcrResultsList(
    result: OcrSearchResult,
    modifier: Modifier = Modifier,
    onRetryCamera: () -> Unit,
    onRetryGallery: () -> Unit
) {
    val ocrSummary = stringResource(
        R.string.ocr_summary,
        result.totalDetected,
        result.found.size
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OcrSummaryCard(
            totalDetected = result.totalDetected,
            foundCount = result.found.size,
            notFoundCount = result.notFoundCodes.size,
            contentDescription = ocrSummary
        )

        if (result.found.isEmpty() && result.totalDetected == 0) {
            OcrHelpCard(
                rawText = result.rawText,
                onRetryCamera = onRetryCamera,
                onRetryGallery = onRetryGallery
            )
            return
        }

        result.found.forEach { additive ->
            ResultCard(additive = additive)
        }

        if (result.notFoundCodes.isNotEmpty()) {
            OcrNotFoundCard(notFoundCodes = result.notFoundCodes)
        }
    }
}

@Composable
private fun OcrSummaryCard(
    totalDetected: Int,
    foundCount: Int,
    notFoundCount: Int,
    contentDescription: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.ocr_result_title),
                fontSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = stringResource(R.string.ocr_result_subtitle),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OcrStatBox(
                    label = stringResource(R.string.ocr_stat_detected),
                    value = totalDetected.toString(),
                    modifier = Modifier.weight(1f)
                )
                OcrStatBox(
                    label = stringResource(R.string.ocr_stat_found),
                    value = foundCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                OcrStatBox(
                    label = stringResource(R.string.ocr_stat_not_found),
                    value = notFoundCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OcrStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun OcrNotFoundCard(notFoundCodes: List<String>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ocr_not_found_title),
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = notFoundCodes.joinToString(", "),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.ocr_not_found_codes_help),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OcrHelpCard(
    rawText: String,
    onRetryCamera: () -> Unit,
    onRetryGallery: () -> Unit
) {
    val hasReadableText = rawText.isNotBlank()
    val title = if (hasReadableText) {
        stringResource(R.string.ocr_no_code_title)
    } else {
        stringResource(R.string.ocr_unreadable_title)
    }
    val message = if (hasReadableText) {
        stringResource(R.string.ocr_no_code_message)
    } else {
        stringResource(R.string.ocr_unreadable_message)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$title. $message"
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                lineHeight = 23.sp
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(15.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.ocr_better_result_title),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.ocr_better_result_tips),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            if (hasReadableText) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.ocr_manual_tip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRetryCamera,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ocr_retry_camera"),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(stringResource(R.string.button_scan_again), fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onRetryGallery,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ocr_retry_gallery"),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        text = stringResource(R.string.button_choose_gallery),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
