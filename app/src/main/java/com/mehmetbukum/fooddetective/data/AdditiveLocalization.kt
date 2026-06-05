package com.mehmetbukum.fooddetective.data

/**
 * Additive serbest metinleri için lokalizasyon erişim katmanı.
 *
 * İngilizce alanlar boşsa veya henüz çevrilmemişse Türkçe alanlara güvenli biçimde düşer.
 * Böylece asset aşamalı olarak çevrilebilir; UI katmanı ayrıca değişmek zorunda kalmaz.
 */
fun Additive.localizedName(isEnglish: Boolean): String {
    return if (isEnglish) {
        name_en.takeIfFilled() ?: name_tr
    } else {
        name_tr
    }
}

fun Additive.localizedFunctionalClass(isEnglish: Boolean): String? {
    return if (isEnglish) {
        functional_class_en.takeIfFilled() ?: functional_class
    } else {
        functional_class
    }
}

fun Additive.localizedHealthStatus(isEnglish: Boolean): String? {
    return if (isEnglish) {
        health_status_en.takeIfFilled() ?: health_status
    } else {
        health_status
    }
}

fun Additive.localizedDescription(isEnglish: Boolean): String? {
    return if (isEnglish) {
        description_en.takeIfFilled() ?: description
    } else {
        description
    }
}

fun Additive.localizedWarning(isEnglish: Boolean): String? {
    return if (isEnglish) {
        warning_en.takeIfFilled() ?: warning
    } else {
        warning
    }
}

private fun String?.takeIfFilled(): String? {
    return this?.takeIf { it.isNotBlank() }
}
