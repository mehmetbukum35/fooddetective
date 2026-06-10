package com.mehmetbukum.fooddetective.data

import java.util.Locale

/**
 * ViewModel'in Room'a doğrudan bağımlı kalmaması için ortak veri erişim sözleşmesi.
 * Unit testlerde fake implementasyon kullanılabilir.
 */
interface AdditiveDataSource {
    suspend fun searchSingle(query: String): Additive?
    suspend fun searchSuggestions(query: String, limit: Int = 20): List<Additive>
    suspend fun searchFromOcr(rawText: String): OcrSearchResult
}

interface AdditivesRemoteDataSource {
    suspend fun getAllAdditives(): List<Additive>
    suspend fun getVersion(): AdditivesVersionResponse
    suspend fun getAdditivesSince(date: String): List<Additive>
}

/**
 * OCR / manuel arama mantığını tek bir yerde toplar.
 * MainActivity sadece bu sınıfı çağırır; SQL detaylarıyla ilgilenmez.
 */
class AdditiveRepository(
    private val dao: AdditiveDao,
    private val remoteDataSource: AdditivesRemoteDataSource? = null
) : AdditiveDataSource {

    /**
     * Online iken sunucu veritabanı ana kaynak kabul edilir.
     *
     * 1) Önce küçük /version cevabı alınır.
     * 2) Sunucu versiyonu yerelde kayıtlı son başarılı versiyonla aynıysa tüm liste indirilmez.
     * 3) Değişiklik varsa tüm liste çekilir ve yerel Room tablosu sunucuyla birebir eşitlenir.
     *
     * Böylece sunucudan silinen kayıtlar da 6 saatlik kontrol geldiğinde yerelden silinir.
     * Sunucuya ulaşılamazsa, API boş/eksik liste döndürürse veya veri tutarsızsa
     * yerel offline veritabanı korunur.
     */
    suspend fun syncFromApi(lastSuccessfulVersionHash: String?): SyncResult {
        val remote = remoteDataSource ?: return SyncResult.Skipped("Uzak veri kaynağı tanımlı değil.")

        return try {
            val remoteVersion = remote.getVersion()
            val remoteHash = remoteVersion.version_hash?.trim().orEmpty()
            val localHash = lastSuccessfulVersionHash?.trim().orEmpty()
            val localTotalCount = dao.getTotalCount()

            if (remoteHash.isNotBlank() && remoteHash == localHash && localTotalCount > 0) {
                return SyncResult.NoChange(remoteVersion)
            }

            val allRows = remote.getAllAdditives()

            if (allRows.isEmpty()) {
                return SyncResult.Skipped("Sunucu boş liste döndürdü; yerel veritabanı korundu.")
            }

            val expectedCount = remoteVersion.total_count
            if (expectedCount > 0 && allRows.size < expectedCount) {
                return SyncResult.Skipped(
                    "Sunucu kayıt sayısı tutarsız; yerel veritabanı korundu. " +
                        "Version kayıt sayısı: $expectedCount, indirilen kayıt sayısı: ${allRows.size}."
                )
            }

            val blankCodeCount = allRows.count { it.code.isBlank() }
            if (blankCodeCount > 0) {
                return SyncResult.Skipped(
                    "Sunucudan kodu boş $blankCodeCount kayıt geldi; yerel veritabanı korundu."
                )
            }

            val duplicateCodes = allRows
                .groupBy { it.code.trim().uppercase(Locale.ROOT) }
                .filterValues { rows -> rows.size > 1 }
                .keys

            if (duplicateCodes.isNotEmpty()) {
                return SyncResult.Skipped(
                    "Sunucudan mükerrer kod geldi; yerel veritabanı korundu: " +
                        duplicateCodes.take(5).joinToString()
                )
            }

            val localRows = allRows.map { it.copy(id = 0) }
            dao.replaceAll(localRows)
            SyncResult.Success(
                updatedCount = allRows.size,
                version = remoteVersion
            )
        } catch (e: Exception) {
            SyncResult.Error(e.localizedMessage ?: "Uzak veritabanı senkronizasyon hatası")
        }
    }

    /**
     * Manuel tek kod araması (mevcut davranışın korunması için).
     */
    override suspend fun searchSingle(query: String): Additive? {
        val normalized = CodeParser.normalizeSingleQuery(query)
        // Önce tam eşleşme
        dao.getAdditiveByCode(normalized)?.let { return it }
        // Tam eşleşme yoksa aralık kontrolü
        return matchAgainstRanges(normalized)
    }

    /**
     * Kullanıcı yazdıkça en yakın kayıtları getirir.
     *
     * Örnekler:
     *  - "160"  -> E160A, E160B...
     *  - "E160" -> E160A, E160B...
     *  - "kar"  -> adında/açıklama sınıfında kar geçen kayıtlar
     */
    override suspend fun searchSuggestions(query: String, limit: Int): List<Additive> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 2) return emptyList()

        val codeQuery = CodeParser.normalizeSingleQuery(trimmedQuery)
        val prefixMatches = dao.searchAdditivesByCodePrefix(codeQuery, limit)
        if (prefixMatches.size >= limit) return prefixMatches

        // Kod araması normalize edilir; kelime araması kullanıcının yazdığı metinle yapılır.
        // Böylece "şeker", "yağ asidi", "renklendirici" gibi Türkçe sorgular bozulmaz.
        val keywordMatches = dao.searchAdditives(trimmedQuery, limit)
        return (prefixMatches + keywordMatches)
            .distinctBy { it.code }
            .sortedWith(
                compareBy<Additive> { additive ->
                    when {
                        additive.code == codeQuery -> 0
                        additive.code.startsWith(codeQuery) -> 1
                        else -> 2
                    }
                }.thenBy { it.code }
            )
            .take(limit)
    }

    /**
     * OCR metninden çıkan kodları ve katkı adlarını veritabanıyla eşleştirir.
     *
     * @return Bulunan maddeler + hiç eşleşmeyen kodların listesi + ham OCR metni.
     */
    override suspend fun searchFromOcr(rawText: String): OcrSearchResult {
        val codes = CodeParser.extractCodes(rawText)

        // 1) E-kodu ile doğrudan eşleşmeler
        val directMatches = if (codes.isNotEmpty()) {
            dao.getAdditivesByCodes(codes)
        } else {
            emptyList()
        }
        val matchedCodes = directMatches.map { it.code }.toHashSet()

        // 2) Tam eşleşmeyen kodlar için aralık kontrolü
        val rangeRows = if (codes.isNotEmpty()) dao.getRangeAdditives() else emptyList()
        val results = directMatches.toMutableList()
        val notFound = mutableListOf<String>()

        codes.filter { it !in matchedCodes }.forEach { code ->
            val rangeHit = rangeRows.firstOrNull { CodeParser.matchesRange(code, it.code) }
            if (rangeHit != null) {
                if (results.none { it.code == rangeHit.code }) results.add(rangeHit)
            } else {
                notFound.add(code)
            }
        }

        // 3) Paketlerde E-kodu yerine yazılan katkı adlarını yakala:
        // name_tr, name_en, aliases_tr, aliases_en alanları OCR metni içinde aranır.
        // Koruyucu/renklendirici gibi genel işlev sınıfı kelimeleri özellikle yok sayılır;
        // aksi halde "Koruyucu atmosferde ambalajlanmıştır" gibi etiket cümleleri yanlış katkı döndürür.
        val nameMatches = findNameMatches(rawText)
        nameMatches.forEach { additive ->
            if (results.none { it.code == additive.code }) results.add(additive)
        }

        val uniqueResults = results.distinctBy { it.code }.sortedBy { it.code }
        val totalDetected = uniqueResults.size + notFound.distinct().size

        return OcrSearchResult(
            found = uniqueResults,
            notFoundCodes = notFound.distinct(),
            totalDetected = totalDetected,
            rawText = rawText
        )
    }

    private suspend fun findNameMatches(rawText: String): List<Additive> {
        val normalizedOcrText = normalizeForNameMatch(rawText)
        if (normalizedOcrText.length < 4) return emptyList()

        return dao.getAllAdditives()
            .filter { additive ->
                additive.searchTerms()
                    .map(::normalizeForNameMatch)
                    .filter { term -> term.length >= MIN_NAME_MATCH_LENGTH }
                    .filterNot(::isGenericNameMatchTerm)
                    .any { term -> containsWholeTerm(normalizedOcrText, term) }
            }
            .distinctBy { it.code }
    }

    private suspend fun matchAgainstRanges(code: String): Additive? {
        return dao.getRangeAdditives().firstOrNull { CodeParser.matchesRange(code, it.code) }
    }

    private fun Additive.searchTerms(): List<String> {
        return buildList {
            add(name_tr)
            add(name_en)
            addAll(aliases_tr.splitAliases())
            addAll(aliases_en.splitAliases())
        }.mapNotNull { it?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun String?.splitAliases(): List<String> {
        return this
            ?.split(';', ',', '|')
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
    }

    private fun normalizeForNameMatch(text: String): String {
        return text
            .lowercase(Locale.ROOT)
            .replace('ı', 'i')
            .replace('ğ', 'g')
            .replace('ü', 'u')
            .replace('ş', 's')
            .replace('ö', 'o')
            .replace('ç', 'c')
            .replace('â', 'a')
            .replace('î', 'i')
            .replace('û', 'u')
            .replace(Regex("""[^a-z0-9\s]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun containsWholeTerm(text: String, term: String): Boolean {
        val pattern = Regex("(^|\\s)${Regex.escape(term)}($|\\s)")
        return pattern.containsMatchIn(text)
    }

    private fun isGenericNameMatchTerm(term: String): Boolean {
        return term in GENERIC_NAME_MATCH_TERMS
    }

    companion object {
        private const val MIN_NAME_MATCH_LENGTH = 4
        private val GENERIC_NAME_MATCH_TERMS = setOf(
            "koruyucu",
            "preservative",
            "renklendirici",
            "colorant",
            "colour",
            "colouring",
            "coloring",
            "tatlandirici",
            "sweetener",
            "antioksidan",
            "antioxidant",
            "emulgator",
            "emulsifier",
            "stabilizor",
            "stabilizer",
            "stabiliser",
            "tasiyici",
            "carrier",
            "asitlik duzenleyici",
            "acidity regulator",
            "topaklanmayi onleyici",
            "anti caking agent",
            "kivam artirici",
            "thickener",
            "jellestirici",
            "gelling agent",
            "parlatici",
            "glazing agent",
            "ambalajlama gazi",
            "packaging gas",
            "itici gaz",
            "propellant"
        )
    }
}

/**
 * OCR arama sonucunu taşır.
 */
data class OcrSearchResult(
    val found: List<Additive>,
    val notFoundCodes: List<String>,
    val totalDetected: Int,
    val rawText: String = ""
)

data class AdditivesVersionResponse(
    val total_count: Int,
    val last_updated: String?,
    val version_hash: String?
)

sealed class SyncResult {
    data class Success(
        val updatedCount: Int,
        val version: AdditivesVersionResponse
    ) : SyncResult()

    data class NoChange(
        val version: AdditivesVersionResponse
    ) : SyncResult()

    data class Skipped(val reason: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
