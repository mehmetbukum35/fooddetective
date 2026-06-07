package com.mehmetbukum.fooddetective

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehmetbukum.fooddetective.data.Additive
import com.mehmetbukum.fooddetective.data.localizedDescription
import com.mehmetbukum.fooddetective.data.localizedFunctionalClass
import com.mehmetbukum.fooddetective.data.localizedHealthStatus
import com.mehmetbukum.fooddetective.data.localizedName
import com.mehmetbukum.fooddetective.data.localizedWarning
import com.mehmetbukum.fooddetective.domain.HalalStatus
import com.mehmetbukum.fooddetective.domain.RiskLevel
import com.mehmetbukum.fooddetective.ui.components.FooterNote
import com.mehmetbukum.fooddetective.ui.components.SectionTitle
import com.mehmetbukum.fooddetective.ui.components.WarningBox
import com.mehmetbukum.fooddetective.ui.components.riskBarCount
import com.mehmetbukum.fooddetective.ui.theme.LocalAppDarkTheme

@Composable
fun ResultCard(additive: Additive, modifier: Modifier = Modifier) {
    val isDarkTheme = LocalAppDarkTheme.current
    val isEnglish = LocalConfiguration.current.locales[0].language == "en"
    val additiveName = additive.localizedName(isEnglish)
    val functionalClass = additive.localizedFunctionalClass(isEnglish)
    val healthStatus = additive.localizedHealthStatus(isEnglish)
    val description = additive.localizedDescription(isEnglish)
    val warning = additive.localizedWarning(isEnglish)
    val riskLevel = RiskLevel.fromRaw(additive.risk_level)
    val halalStatus = HalalStatus.fromRaw(additive.halal_status)
    val riskLabel = stringResource(riskLevel.labelRes)
    val halalLabel = stringResource(halalStatus.labelRes)
    val riskColor by animateColorAsState(
        targetValue = riskColor(riskLevel, isDarkTheme),
        label = "Risk Color"
    )
    val purpose = functionalClass ?: stringResource(R.string.no_info)
    val resultDescription = stringResource(
        R.string.a11y_result_card,
        additive.code,
        additiveName,
        purpose,
        halalLabel,
        riskLabel
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = resultDescription
            },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            ResultHeroHeader(
                code = additive.code,
                name = additiveName,
                category = functionalClass,
                halalStatus = halalStatus,
                halalLabel = halalLabel,
                riskLevel = riskLevel,
                riskLabel = riskLabel,
                riskColor = riskColor,
                isDarkTheme = isDarkTheme,
                isEnglish = isEnglish
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 17.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkTheme) 0.38f else 0.22f)
            )

            InfoGrid(
                additive = additive,
                functionalClass = functionalClass,
                healthStatus = healthStatus,
                description = description,
                warning = warning,
                riskColor = riskColor,
                halalLabel = halalLabel,
                riskLabel = riskLabel,
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(18.dp))

            FooterNote()
        }
    }
}

@Composable
private fun ResultHeroHeader(
    code: String,
    name: String,
    category: String?,
    halalStatus: HalalStatus,
    halalLabel: String,
    riskLevel: RiskLevel,
    riskLabel: String,
    riskColor: Color,
    isDarkTheme: Boolean,
    isEnglish: Boolean
) {
    var showRiskInfo by remember { mutableStateOf(false) }
    val heroBackground = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    }

    if (showRiskInfo) {
        RiskInfoDialog(
            isEnglish = isEnglish,
            onDismiss = { showRiskInfo = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = heroBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.result_card_label),
                        fontSize = 10.sp,
                        letterSpacing = 2.0.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = code,
                        fontSize = 38.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 23.sp
                    )
                    category?.takeIf { it.isNotBlank() }?.let { value ->
                        Spacer(modifier = Modifier.height(7.dp))
                        Text(
                            text = value.uppercase(),
                            fontSize = 10.sp,
                            letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                RiskBadge(
                    riskLevel = riskLevel,
                    riskLabel = riskLabel,
                    riskColor = riskColor,
                    isDarkTheme = isDarkTheme,
                    onClick = { showRiskInfo = true }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            StatusPill(
                title = stringResource(R.string.label_halal),
                value = halalLabel,
                color = halalColor(halalStatus, isDarkTheme),
                background = halalContainerColor(halalStatus, isDarkTheme),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RiskInfoDialog(
    isEnglish: Boolean,
    onDismiss: () -> Unit
) {
    val title = if (isEnglish) "How is the risk level estimated?" else stringResource(R.string.risk_info_title)
    val body = if (isEnglish) {
        "The risk bars summarize the available database notes for this additive. They are only a quick guide. Please also check the product label and reliable sources when needed."
    } else {
        stringResource(R.string.risk_info_body)
    }
    val closeText = if (isEnglish) "Got it" else stringResource(R.string.about_dialog_close)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Text(
                text = body,
                lineHeight = 21.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(closeText)
            }
        }
    )
}

@Composable
private fun StatusPill(
    title: String,
    value: String,
    color: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Column {
            Text(
                text = title,
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.0.sp,
                maxLines = 1
            )
            Text(
                text = value,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InfoGrid(
    additive: Additive,
    functionalClass: String?,
    healthStatus: String?,
    description: String?,
    warning: String?,
    riskColor: Color,
    halalLabel: String,
    riskLabel: String,
    isDarkTheme: Boolean
) {
    val unknownPurpose = stringResource(R.string.no_info)
    val healthNoteEmpty = stringResource(R.string.health_note_empty)
    val purposeBackground = if (isDarkTheme) DarkPurposeBox else MaterialTheme.colorScheme.surfaceVariant
    val purposeTextColor = if (isDarkTheme) DarkMintText else MaterialTheme.colorScheme.onSurfaceVariant
    val purposeTitleColor = if (isDarkTheme) DarkMutedText else MaterialTheme.colorScheme.onSurfaceVariant
    val healthBackground = if (isDarkTheme) DarkHealthBox else MaterialTheme.colorScheme.surfaceVariant
    val healthTextColor = if (isDarkTheme) DarkWarningText else MaterialTheme.colorScheme.onSurfaceVariant
    val healthTitleColor = if (isDarkTheme) DarkAmberTitle else MaterialTheme.colorScheme.onSurfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoBox(
            title = stringResource(R.string.label_purpose),
            value = functionalClass ?: unknownPurpose,
            modifier = Modifier.fillMaxWidth(),
            background = purposeBackground,
            valueColor = purposeTextColor,
            titleColor = purposeTitleColor
        )

        InfoBox(
            title = stringResource(R.string.label_health_note),
            value = healthStatus ?: warning ?: healthNoteEmpty,
            modifier = Modifier.fillMaxWidth(),
            background = healthBackground,
            valueColor = healthTextColor,
            titleColor = healthTitleColor,
            sideColor = riskColor
        )

        SectionTitle(stringResource(R.string.section_short_description))
        Text(
            text = description ?: shortResultSummary(
                additive = additive,
                functionalClass = functionalClass,
                defaultPurpose = stringResource(R.string.summary_default_purpose),
                defaultHalal = stringResource(R.string.summary_unknown_halal),
                defaultRisk = stringResource(R.string.summary_unknown_risk),
                template = stringResource(R.string.summary_template),
                halalLabel = halalLabel,
                riskLabel = riskLabel
            ),
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (!warning.isNullOrBlank() && warning != healthStatus) {
            WarningBox(text = warning)
        }
    }
}

@Composable
private fun InfoBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    background: Color,
    valueColor: Color,
    titleColor: Color,
    sideColor: Color? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (sideColor != null) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(88.dp)
                        .background(sideColor)
                )
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    letterSpacing = 1.8.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = value,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor
                )
            }
        }
    }
}

@Composable
private fun RiskBadge(
    riskLevel: RiskLevel,
    riskLabel: String,
    riskColor: Color,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .background(riskContainerColor(riskLevel, isDarkTheme))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.label_risk),
            color = riskColor,
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            letterSpacing = 1.1.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        CompactSignalBars(activeBars = riskBarCount(riskLevel), activeColor = riskColor)

        Text(
            text = riskLabel,
            color = riskColor,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CompactSignalBars(activeBars: Int, activeColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(9.dp, 15.dp, 21.dp).forEachIndexed { index, height ->
            val isActive = index < activeBars
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(height)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isActive) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            )
        }
    }
}

private fun shortResultSummary(
    additive: Additive,
    functionalClass: String?,
    defaultPurpose: String,
    defaultHalal: String,
    defaultRisk: String,
    template: String,
    halalLabel: String,
    riskLabel: String
): String {
    val purpose = functionalClass?.takeIf { it.isNotBlank() } ?: defaultPurpose
    val halal = halalLabel.takeIf { it.isNotBlank() } ?: defaultHalal
    val risk = riskLabel.takeIf { it.isNotBlank() } ?: defaultRisk
    return template.format(additive.code, purpose, halal, risk)
}
