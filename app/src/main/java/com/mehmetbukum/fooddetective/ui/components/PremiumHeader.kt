package com.mehmetbukum.fooddetective.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehmetbukum.fooddetective.R
import com.mehmetbukum.fooddetective.ui.theme.LocalAppDarkTheme

@Composable
fun PremiumHeader(
    trailing: @Composable () -> Unit = {}
) {
    val headerDescription = stringResource(R.string.a11y_header_description)
    val isDarkTheme = LocalAppDarkTheme.current
    val headerBrush = if (isDarkTheme) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF061B24),
                Color(0xFF0B2C2E),
                Color(0xFF123C35)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF8FB9A7),
                Color(0xFF4F8B73),
                Color(0xFF123C35)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(245.dp)
            .background(headerBrush)
            .semantics(mergeDescendants = true) {
                contentDescription = headerDescription
            }
            .padding(horizontal = 28.dp, vertical = 36.dp)
    ) {
        AnimatedHeaderSpace(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = stringResource(R.string.header_label),
                fontSize = 11.sp,
                letterSpacing = 3.2.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkTheme) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF123C35)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.header_title),
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.header_subtitle),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Color.White.copy(alpha = 0.88f)
            )
        }

        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            trailing()
        }
    }
}
