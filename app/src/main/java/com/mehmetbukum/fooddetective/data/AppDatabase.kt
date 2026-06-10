package com.mehmetbukum.fooddetective.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
                    DATABASE_NAME
                )
                    .createFromAsset("database/e_katki_maddeleri_sade.sqlite")
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private const val DATABASE_NAME = "food_detective_database"
        private const val ADDITIVES_TABLE = "additives"
        private const val ADDITIVES_MIGRATION_TABLE = "additives_migration_tmp"

        val MIGRATION_1_2 = additiveTableMigration(1, 2)
        val MIGRATION_2_3 = additiveTableMigration(2, 3)
        val MIGRATION_3_4 = additiveTableMigration(3, 4)
        val MIGRATION_4_5 = additiveTableMigration(4, 5)
        val MIGRATION_5_6 = additiveTableMigration(5, 6)

        private fun additiveTableMigration(startVersion: Int, endVersion: Int): Migration {
            return object : Migration(startVersion, endVersion) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    migrateAdditivesTableToCurrentSchema(db)
                }
            }
        }

        /**
         * Rebuilds the additive asset table into the current Room schema while preserving
         * any columns that already exist in older database versions.
         *
         * The table does not contain user-generated data today, but using explicit
         * migrations keeps future user data safe and avoids destructive migrations.
         */
        private fun migrateAdditivesTableToCurrentSchema(db: SupportSQLiteDatabase) {
            if (!db.hasTable(ADDITIVES_TABLE)) {
                db.createCurrentAdditivesTable(ADDITIVES_TABLE)
                db.createAdditivesCodeIndex()
                return
            }

            val columns = db.columnNames(ADDITIVES_TABLE)
            db.execSQL("DROP TABLE IF EXISTS $ADDITIVES_MIGRATION_TABLE")
            db.createCurrentAdditivesTable(ADDITIVES_MIGRATION_TABLE)

            db.execSQL(
                """
                INSERT INTO $ADDITIVES_MIGRATION_TABLE (
                    id, code, name_tr, functional_class, halal_status, health_status,
                    risk_level, description, warning, name_en, functional_class_en,
                    health_status_en, description_en, warning_en, aliases_tr, aliases_en, updated_at
                )
                SELECT
                    ${columns.intColumnOrNull("id")} AS id,
                    ${columns.requiredTextColumn("code")} AS code,
                    ${columns.requiredTextColumn("name_tr")} AS name_tr,
                    ${columns.nullableTextColumn("functional_class")} AS functional_class,
                    ${columns.nullableTextColumn("halal_status")} AS halal_status,
                    ${columns.nullableTextColumn("health_status")} AS health_status,
                    ${columns.nullableTextColumn("risk_level")} AS risk_level,
                    ${columns.nullableTextColumn("description")} AS description,
                    ${columns.nullableTextColumn("warning")} AS warning,
                    ${columns.nullableTextColumn("name_en")} AS name_en,
                    ${columns.nullableTextColumn("functional_class_en")} AS functional_class_en,
                    ${columns.nullableTextColumn("health_status_en")} AS health_status_en,
                    ${columns.nullableTextColumn("description_en")} AS description_en,
                    ${columns.nullableTextColumn("warning_en")} AS warning_en,
                    ${columns.nullableTextColumn("aliases_tr")} AS aliases_tr,
                    ${columns.nullableTextColumn("aliases_en")} AS aliases_en,
                    ${columns.nullableTextColumn("updated_at")} AS updated_at
                FROM $ADDITIVES_TABLE
                """.trimIndent()
            )

            db.execSQL("DROP TABLE $ADDITIVES_TABLE")
            db.execSQL("ALTER TABLE $ADDITIVES_MIGRATION_TABLE RENAME TO $ADDITIVES_TABLE")
            db.createAdditivesCodeIndex()
        }

        private fun SupportSQLiteDatabase.hasTable(tableName: String): Boolean {
            query(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
                arrayOf(tableName)
            ).use { cursor ->
                return cursor.moveToFirst()
            }
        }

        private fun SupportSQLiteDatabase.columnNames(tableName: String): Set<String> {
            query("PRAGMA table_info($tableName)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                val result = mutableSetOf<String>()
                while (cursor.moveToNext()) {
                    result += cursor.getString(nameIndex)
                }
                return result
            }
        }

        private fun SupportSQLiteDatabase.createCurrentAdditivesTable(tableName: String) {
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS $tableName (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    code TEXT NOT NULL,
                    name_tr TEXT NOT NULL,
                    functional_class TEXT,
                    halal_status TEXT,
                    health_status TEXT,
                    risk_level TEXT,
                    description TEXT,
                    warning TEXT,
                    name_en TEXT,
                    functional_class_en TEXT,
                    health_status_en TEXT,
                    description_en TEXT,
                    warning_en TEXT,
                    aliases_tr TEXT,
                    aliases_en TEXT,
                    updated_at TEXT
                )
                """.trimIndent()
            )
        }

        private fun SupportSQLiteDatabase.createAdditivesCodeIndex() {
            execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_additives_code ON $ADDITIVES_TABLE(code)")
        }

        private fun Set<String>.intColumnOrNull(columnName: String): String {
            return if (columnName in this) columnName else "NULL"
        }

        private fun Set<String>.requiredTextColumn(columnName: String): String {
            return if (columnName in this) "COALESCE($columnName, '')" else "''"
        }

        private fun Set<String>.nullableTextColumn(columnName: String): String {
            return if (columnName in this) columnName else "NULL"
        }
    }
}
