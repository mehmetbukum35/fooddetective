package com.mehmetbukum.fooddetective.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Additive::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun additiveDao(): AdditiveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_detective_database"
                )
                    .createFromAsset("database/e_katki_maddeleri_sade.sqlite")
                    // Bu veritabanı kullanıcı verisi içermez; uygulamayla gelen salt-okunur
                    // katkı maddesi asset'idir. Şema/asset değiştiğinde eski yerel kopyanın
                    // silinip yeni asset'ten yeniden kurulması bilinçli ve güvenli tercihtir.
                    // Kullanıcı favorileri/geçmiş arama gibi veri eklenirse gerçek migration yazılmalıdır.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
