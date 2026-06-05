package com.mehmetbukum.fooddetective

import androidx.compose.ui.graphics.Color
import com.mehmetbukum.fooddetective.domain.HalalStatus
import com.mehmetbukum.fooddetective.domain.RiskLevel

val AppDeepGreen = Color(0xFF1A3A2A)
val AppGreen = Color(0xFF2D5A3D)
val AppMint = Color(0xFFA7F3D0)
val AppSoftMint = Color(0xFFF0FDF4)
val AppIvory = Color(0xFFF8F5EF)
val AppSoftLine = Color(0xFFE5E7EB)
val AppMuted = Color(0xFF6B7280)

val DarkSurfaceSoft = Color(0xFF162C25)
val DarkPurposeBox = Color(0xFF182C25)
val DarkHealthBox = Color(0xFF2D2616)
val DarkDangerBox = Color(0xFF2A1718)
val DarkRiskBox = Color(0xFF35181B)
val DarkMutedText = Color(0xFFA8BDB6)
val DarkMintText = Color(0xFFDDF7F0)
val DarkWarningText = Color(0xFFEDE2C0)
val DarkAmberTitle = Color(0xFFE6C678)

private val RiskLow = Color(0xFF22C55E)
private val RiskMedium = Color(0xFFF59E0B)
private val RiskHigh = Color(0xFFEF4444)
private val Unknown = Color(0xFF9CA3AF)

private val DarkRiskLow = Color(0xFF8DD8A8)
private val DarkRiskMedium = Color(0xFFE6C678)
private val DarkRiskHigh = Color(0xFFFF8A8A)
private val DarkUnknown = Color(0xFFA8BDB6)

fun riskColor(riskLevel: RiskLevel): Color {
    return riskColor(riskLevel, isDark = false)
}

fun riskColor(riskLevel: RiskLevel, isDark: Boolean): Color {
    return when (riskLevel) {
        RiskLevel.LOW -> if (isDark) DarkRiskLow else RiskLow
        RiskLevel.MEDIUM -> if (isDark) DarkRiskMedium else RiskMedium
        RiskLevel.HIGH -> if (isDark) DarkRiskHigh else RiskHigh
        RiskLevel.UNKNOWN -> if (isDark) DarkUnknown else Unknown
    }
}

fun riskContainerColor(riskLevel: RiskLevel): Color {
    return riskContainerColor(riskLevel, isDark = false)
}

fun riskContainerColor(riskLevel: RiskLevel, isDark: Boolean): Color {
    if (isDark) {
        return when (riskLevel) {
            RiskLevel.LOW -> Color(0xFF173322)
            RiskLevel.MEDIUM -> Color(0xFF2D2616)
            RiskLevel.HIGH -> DarkRiskBox
            RiskLevel.UNKNOWN -> Color(0xFF1D2724)
        }
    }

    return when (riskLevel) {
        RiskLevel.LOW -> Color(0xFFDCFCE7)
        RiskLevel.MEDIUM -> Color(0xFFFEF3C7)
        RiskLevel.HIGH -> Color(0xFFFEE2E2)
        RiskLevel.UNKNOWN -> Color(0xFFF3F4F6)
    }
}

fun halalColor(status: HalalStatus): Color {
    return halalColor(status, isDark = false)
}

fun halalColor(status: HalalStatus, isDark: Boolean): Color {
    return when (status) {
        HalalStatus.HALAL -> if (isDark) Color(0xFF8DD8A8) else Color(0xFF16A34A)
        HalalStatus.HARAM -> if (isDark) Color(0xFFFF8A8A) else Color(0xFFDC2626)
        HalalStatus.SUSPICIOUS -> if (isDark) Color(0xFFE6C678) else Color(0xFFD97706)
        HalalStatus.UNKNOWN -> if (isDark) DarkUnknown else Color(0xFF6B7280)
    }
}

fun halalIconColor(status: HalalStatus, isDark: Boolean): Color {
    if (!isDark) return halalColor(status, isDark = false)

    return when (status) {
        HalalStatus.HALAL -> Color(0xFF22C55E)
        HalalStatus.HARAM -> Color(0xFFE51E2A)
        HalalStatus.SUSPICIOUS -> Color(0xFFD97706)
        HalalStatus.UNKNOWN -> Color(0xFF64748B)
    }
}

fun halalContainerColor(status: HalalStatus): Color {
    return halalContainerColor(status, isDark = false)
}

fun halalContainerColor(status: HalalStatus, isDark: Boolean): Color {
    if (isDark) {
        return when (status) {
            HalalStatus.HALAL -> Color(0xFF173322)
            HalalStatus.HARAM -> DarkDangerBox
            HalalStatus.SUSPICIOUS -> Color(0xFF2D2616)
            HalalStatus.UNKNOWN -> Color(0xFF1D2724)
        }
    }

    return when (status) {
        HalalStatus.HALAL -> Color(0xFFF0FDF4)
        HalalStatus.HARAM -> Color(0xFFFFF1F2)
        HalalStatus.SUSPICIOUS -> Color(0xFFFFFBEB)
        HalalStatus.UNKNOWN -> Color(0xFFF3F4F6)
    }
}
