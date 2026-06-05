package com.mehmetbukum.fooddetective.domain

import androidx.annotation.StringRes
import com.mehmetbukum.fooddetective.R
import java.util.Locale

enum class RiskLevel(@StringRes val labelRes: Int) {
    LOW(R.string.risk_low),
    MEDIUM(R.string.risk_medium),
    HIGH(R.string.risk_high),
    UNKNOWN(R.string.risk_unknown);

    companion object {
        fun fromRaw(raw: String?): RiskLevel {
            return when (raw.normalizedStatus()) {
                "düşük", "dusuk", "low" -> LOW
                "orta", "medium" -> MEDIUM
                "yüksek", "yuksek", "high" -> HIGH
                else -> UNKNOWN
            }
        }
    }
}

enum class HalalStatus(@StringRes val labelRes: Int) {
    HALAL(R.string.halal_halal),
    HARAM(R.string.halal_haram),
    SUSPICIOUS(R.string.halal_suspicious),
    UNKNOWN(R.string.halal_unknown);

    companion object {
        fun fromRaw(raw: String?): HalalStatus {
            return when (raw.normalizedStatus()) {
                "helal", "halal" -> HALAL
                "haram" -> HARAM
                "şüpheli", "supheli", "suspicious", "doubtful" -> SUSPICIOUS
                else -> UNKNOWN
            }
        }
    }
}

private fun String?.normalizedStatus(): String {
    return this
        ?.trim()
        ?.lowercase(Locale.ROOT)
        ?.replace('ı', 'i')
        ?.replace('ğ', 'g')
        ?.replace('ü', 'u')
        ?.replace('ş', 's')
        ?.replace('ö', 'o')
        ?.replace('ç', 'c')
        .orEmpty()
}
