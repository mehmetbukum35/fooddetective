package com.mehmetbukum.fooddetective.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdditiveLocalizationTest {

    @Test
    fun localizedName_returnsTurkishNameForDefaultLanguage() {
        val additive = additive(nameEn = "Citric Acid")

        assertEquals("Sitrik Asit", additive.localizedName(isEnglish = false))
    }

    @Test
    fun localizedName_returnsEnglishNameWhenAvailable() {
        val additive = additive(nameEn = "Citric Acid")

        assertEquals("Citric Acid", additive.localizedName(isEnglish = true))
    }

    @Test
    fun localizedName_fallsBackToTurkishNameWhenEnglishNameIsBlank() {
        val additive = additive(nameEn = "  ")

        assertEquals("Sitrik Asit", additive.localizedName(isEnglish = true))
    }

    @Test
    fun localizedFunctionalClass_returnsEnglishWhenAvailable() {
        val additive = additive(
            functionalClass = "Asitlik düzenleyici",
            functionalClassEn = "Acidity regulator"
        )

        assertEquals("Asitlik düzenleyici", additive.localizedFunctionalClass(isEnglish = false))
        assertEquals("Acidity regulator", additive.localizedFunctionalClass(isEnglish = true))
    }

    @Test
    fun localizedOptionalFields_preserveNullFallbacks() {
        val additive = additive(
            functionalClass = null,
            healthStatus = null,
            description = null,
            warning = null,
            functionalClassEn = null,
            healthStatusEn = null,
            descriptionEn = null,
            warningEn = null
        )

        assertNull(additive.localizedFunctionalClass(isEnglish = true))
        assertNull(additive.localizedHealthStatus(isEnglish = true))
        assertNull(additive.localizedDescription(isEnglish = true))
        assertNull(additive.localizedWarning(isEnglish = true))
    }

    private fun additive(
        functionalClass: String? = "Asitlik düzenleyici",
        healthStatus: String? = "Genel kullanımda düşük risklidir.",
        description: String? = "Test açıklaması",
        warning: String? = "Test uyarısı",
        nameEn: String? = null,
        functionalClassEn: String? = null,
        healthStatusEn: String? = null,
        descriptionEn: String? = null,
        warningEn: String? = null
    ): Additive {
        return Additive(
            code = "E330",
            name_tr = "Sitrik Asit",
            functional_class = functionalClass,
            halal_status = "Helal",
            health_status = healthStatus,
            risk_level = "Düşük",
            description = description,
            warning = warning,
            name_en = nameEn,
            functional_class_en = functionalClassEn,
            health_status_en = healthStatusEn,
            description_en = descriptionEn,
            warning_en = warningEn,
            aliases_tr = null,
            aliases_en = null,
            updated_at = "2026-01-01"
        )
    }
}
