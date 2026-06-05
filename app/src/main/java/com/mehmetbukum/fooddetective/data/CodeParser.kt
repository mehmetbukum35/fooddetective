package com.mehmetbukum.fooddetective.data

/**
 * OCR ile çıkarılan ham metinden E-kodlarını ayıklar, normalize eder ve
 * veritabanı eşleştirmesi için yardımcı fonksiyonlar sağlar.
 *
 * Veritabanındaki kod formatları (gerçek veriye göre):
 *  - E100, E102 ...           (E + 3-4 rakam)
 *  - E160A, E553B, E440B ...  (E + rakam + tek harf ekli)
 *  - E1400-E1450 ...          (aralık kodu, tek satır olarak saklanır)
 */
object CodeParser {

    /**
     * Kesin E-kodu yakalama.
     * Örn: "E102", "E 102", "E-330", "e160a"
     */
    private val CODE_REGEX = Regex(
        pattern = """\bE[\s\-]?(\d{3,4})\s?([A-Z])?\b""",
        option = RegexOption.IGNORE_CASE
    )

    /**
     * OCR bazen E harfini C/F/£/€ gibi okuyabilir.
     * Bu desen sadece sayıdan hemen önce gelen tek karakterli şüpheli okumaları yakalar.
     * Sonuçlar doğrudan metinden gelmiş gibi değil, kontrollü biçimde E-koduna normalize edilir.
     */
    private val SUSPECT_CODE_REGEX = Regex(
        pattern = """\b[CF£€][\s\-]?(\d{3,4})\s?([A-Z])?\b""",
        option = RegexOption.IGNORE_CASE
    )

    /**
     * OCR'ın sık karıştırdığı karakterleri düzeltir.
     */
    private fun preClean(raw: String): String {
        return raw
            .replace('É', 'E').replace('È', 'E').replace('Ê', 'E').replace('Ë', 'E')
            .replace('é', 'e').replace('è', 'e').replace('ê', 'e').replace('ë', 'e')
            .replace('€', 'E').replace('£', 'E')
            .replace('\n', ' ')
            .replace(Regex("""\s+"""), " ")
    }

    /**
     * Ham OCR metninden benzersiz, normalize edilmiş E-kodu listesi döndürür.
     * Örn: ["E102", "E160A", "E330"]
     */
    fun extractCodes(rawText: String): List<String> {
        if (rawText.isBlank()) return emptyList()

        val cleaned = preClean(rawText)
        val found = LinkedHashSet<String>()

        CODE_REGEX.findAll(cleaned).forEach { match ->
            found.add(buildCode(match.groupValues[1], match.groupValues[2]))
        }

        // İkinci geçiş: C160 / F160 gibi OCR şüphelilerini de kontrollü şekilde yakala.
        SUSPECT_CODE_REGEX.findAll(cleaned).forEach { match ->
            found.add(buildCode(match.groupValues[1], match.groupValues[2]))
        }

        return found.toList()
    }

    /**
     * Kullanıcının manuel girdiği tek bir sorguyu normalize eder.
     *  "102"   -> "E102"
     *  "e160a" -> "E160A"
     *  "E 330" -> "E330"
     */
    fun normalizeSingleQuery(query: String): String {
        val q = query
            .trim()
            .uppercase()
            .replace(" ", "")
            .replace("-", "")
            .replace("€", "E")
            .replace("£", "E")
        return if (q.all { it.isDigit() }) "E$q" else q
    }

    /**
     * Doğrudan eşleşmeyen bir kodun, aralık kodlarından (E1400-E1450) birine
     * düşüp düşmediğini kontrol eder.
     *
     * @param code     Aranan tek kod (örn "E1410")
     * @param rangeRow Veritabanından gelen aralık satırının code alanı (örn "E1400-E1450")
     * @return Kod aralığa giriyorsa true.
     */
    fun matchesRange(code: String, rangeRow: String): Boolean {
        // "E1400-E1450" -> 1400 ve 1450
        val parts = rangeRow.uppercase().replace("E", "").split("-")
        if (parts.size != 2) return false

        val low = parts[0].trim().toIntOrNull() ?: return false
        val high = parts[1].trim().toIntOrNull() ?: return false

        // Aranan koddan sadece sayısal kısmı al (harf ekini at): "E1410" -> 1410
        val num = code.uppercase()
            .replace("E", "")
            .takeWhile { it.isDigit() }
            .toIntOrNull() ?: return false

        return num in low..high
    }

    private fun buildCode(digits: String, letter: String): String {
        return "E$digits${letter.uppercase()}"
    }
}
