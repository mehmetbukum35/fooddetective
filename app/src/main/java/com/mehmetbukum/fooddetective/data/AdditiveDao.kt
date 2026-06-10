package com.mehmetbukum.fooddetective.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class AdditiveDao {
    // Kodu tam eşleşen maddeyi getirir (Örn: "E100")
    @Query("SELECT * FROM additives WHERE code = :searchCode LIMIT 1")
    abstract suspend fun getAdditiveByCode(searchCode: String): Additive?

    // Kodun başına göre öneri getirir. Örn: E160 -> E160A, E160B, E160C...
    @Query("""
        SELECT * FROM additives
        WHERE code LIKE :searchCode || '%'
        ORDER BY code ASC
        LIMIT :limit
    """)
    abstract suspend fun searchAdditivesByCodePrefix(searchCode: String, limit: Int = 20): List<Additive>

    // İçinde aranılan kelime/kod geçenleri liste halinde getirir
    @Query("""
        SELECT * FROM additives
        WHERE code LIKE '%' || :searchCode || '%'
           OR name_tr LIKE '%' || :searchCode || '%'
           OR name_en LIKE '%' || :searchCode || '%'
           OR aliases_tr LIKE '%' || :searchCode || '%'
           OR aliases_en LIKE '%' || :searchCode || '%'
           OR functional_class LIKE '%' || :searchCode || '%'
           OR functional_class_en LIKE '%' || :searchCode || '%'
        ORDER BY code ASC
        LIMIT :limit
    """)
    abstract suspend fun searchAdditives(searchCode: String, limit: Int = 20): List<Additive>

    // OCR'dan çıkan birden fazla kodu tek seferde sorgular
    @Query("SELECT * FROM additives WHERE code IN (:codes)")
    abstract suspend fun getAdditivesByCodes(codes: List<String>): List<Additive>

    // OCR isim eşleştirmesi için aday kayıtları getirir.
    // Tüm tablo küçük olduğu için Kotlin tarafında normalize ederek eşleştirmek daha güvenlidir.
    @Query("SELECT * FROM additives")
    abstract suspend fun getAllAdditives(): List<Additive>

    // Aralık kodlarını (E1400-E1450 gibi) bulmak için tüm aralık satırlarını çeker
    @Query("SELECT * FROM additives WHERE code LIKE '%-%'")
    abstract suspend fun getRangeAdditives(): List<Additive>

    // API'den gelen kayıtları ekler/günceller.
    // Remote sync sırasında id sıfırlanır; Room yerel id üretir, code unique index'i iş kimliğini korur.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAll(additives: List<Additive>)

    @Query("DELETE FROM additives")
    abstract suspend fun deleteAll()

    // Online senkronizasyonda sunucu ana kaynak kabul edilir.
    // Bu işlem transaction içindedir: insert başarısız olursa eski yerel veri korunur.
    @Transaction
    open suspend fun replaceAll(additives: List<Additive>) {
        deleteAll()
        upsertAll(additives)
    }

    // Yereldeki en son güncelleme zamanı
    @Query("SELECT MAX(updated_at) FROM additives")
    abstract suspend fun getLastUpdatedAt(): String?

    // Yereldeki toplam kayıt sayısı
    @Query("SELECT COUNT(*) FROM additives")
    abstract suspend fun getTotalCount(): Int
}
