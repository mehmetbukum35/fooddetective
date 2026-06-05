package com.mehmetbukum.fooddetective.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehmetbukum.fooddetective.DarkMutedText
import com.mehmetbukum.fooddetective.R
import com.mehmetbukum.fooddetective.domain.RiskLevel
import com.mehmetbukum.fooddetective.ui.theme.LocalAppDarkTheme

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.loading_label_reading_tag),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorCard(message: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.error_title_operation_failed),
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
fun NotFoundText(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.not_found_title),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SmallStatusPill(text: String, color: Color, background: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 6.dp, bottom = 3.dp)
    )
}

@Composable
fun WarningBox(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.warning_box_title),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun FooterNote() {
    val isDarkTheme = LocalAppDarkTheme.current
    Text(
        text = stringResource(R.string.footer_general_info_note),
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = if (isDarkTheme) {
            DarkMutedText.copy(alpha = 0.86f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
        },
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 10.dp)
    )
}

@Composable
fun SignalBars(activeBars: Int, activeColor: Color) {
    Row(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(14.dp, 21.dp, 28.dp).forEachIndexed { index, height ->
            val isActive = index < activeBars
            Box(
                modifier = Modifier
                    .width(7.dp)
                    .height(height)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (isActive) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            )
        }
    }
}

fun riskBarCount(riskLevel: RiskLevel): Int {
    return when (riskLevel) {
        RiskLevel.LOW -> 1
        RiskLevel.MEDIUM -> 2
        RiskLevel.HIGH -> 3
        RiskLevel.UNKNOWN -> 0
    }
}
