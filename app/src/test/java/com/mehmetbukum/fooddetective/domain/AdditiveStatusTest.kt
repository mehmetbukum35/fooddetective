package com.mehmetbukum.fooddetective.domain

import com.mehmetbukum.fooddetective.R
import org.junit.Assert.assertEquals
import org.junit.Test

class AdditiveStatusTest {

    @Test
    fun riskLevel_fromRaw_mapsKnownTurkishValues() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromRaw("Düşük"))
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromRaw("Orta"))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromRaw("Yüksek"))
    }

    @Test
    fun riskLevel_fromRaw_mapsNormalizedAndEnglishValues() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromRaw("dusuk"))
        assertEquals(RiskLevel.LOW, RiskLevel.fromRaw("low"))
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromRaw("medium"))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromRaw("yuksek"))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromRaw("high"))
    }

    @Test
    fun riskLevel_fromRaw_trimsInput() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromRaw("  Düşük  "))
    }

    @Test
    fun riskLevel_fromRaw_returnsUnknownForNullBlankOrUnexpectedValues() {
        assertEquals(RiskLevel.UNKNOWN, RiskLevel.fromRaw(null))
        assertEquals(RiskLevel.UNKNOWN, RiskLevel.fromRaw(""))
        assertEquals(RiskLevel.UNKNOWN, RiskLevel.fromRaw("very high"))
    }

    @Test
    fun riskLevel_labelResourcesAreStable() {
        assertEquals(R.string.risk_low, RiskLevel.LOW.labelRes)
        assertEquals(R.string.risk_medium, RiskLevel.MEDIUM.labelRes)
        assertEquals(R.string.risk_high, RiskLevel.HIGH.labelRes)
        assertEquals(R.string.risk_unknown, RiskLevel.UNKNOWN.labelRes)
    }

    @Test
    fun halalStatus_fromRaw_mapsKnownTurkishValues() {
        assertEquals(HalalStatus.HALAL, HalalStatus.fromRaw("Helal"))
        assertEquals(HalalStatus.HARAM, HalalStatus.fromRaw("Haram"))
        assertEquals(HalalStatus.SUSPICIOUS, HalalStatus.fromRaw("Şüpheli"))
    }

    @Test
    fun halalStatus_fromRaw_mapsNormalizedAndEnglishValues() {
        assertEquals(HalalStatus.HALAL, HalalStatus.fromRaw("halal"))
        assertEquals(HalalStatus.HARAM, HalalStatus.fromRaw("haram"))
        assertEquals(HalalStatus.SUSPICIOUS, HalalStatus.fromRaw("supheli"))
        assertEquals(HalalStatus.SUSPICIOUS, HalalStatus.fromRaw("suspicious"))
        assertEquals(HalalStatus.SUSPICIOUS, HalalStatus.fromRaw("doubtful"))
    }

    @Test
    fun halalStatus_fromRaw_trimsInput() {
        assertEquals(HalalStatus.SUSPICIOUS, HalalStatus.fromRaw("  Şüpheli  "))
    }

    @Test
    fun halalStatus_fromRaw_returnsUnknownForNullBlankOrUnexpectedValues() {
        assertEquals(HalalStatus.UNKNOWN, HalalStatus.fromRaw(null))
        assertEquals(HalalStatus.UNKNOWN, HalalStatus.fromRaw(""))
        assertEquals(HalalStatus.UNKNOWN, HalalStatus.fromRaw("partly halal"))
    }

    @Test
    fun halalStatus_labelResourcesAreStable() {
        assertEquals(R.string.halal_halal, HalalStatus.HALAL.labelRes)
        assertEquals(R.string.halal_haram, HalalStatus.HARAM.labelRes)
        assertEquals(R.string.halal_suspicious, HalalStatus.SUSPICIOUS.labelRes)
        assertEquals(R.string.halal_unknown, HalalStatus.UNKNOWN.labelRes)
    }
}
