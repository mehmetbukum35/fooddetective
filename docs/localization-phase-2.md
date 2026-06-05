# Localization Phase 2: English Additive Text Fields

The app is now prepared for localization in two layers:

1. Closed domain values such as risk and halal status are represented by enums.
2. Free-text additive fields are routed through localization helper functions.

The next step is to add optional English columns to the bundled SQLite asset.

## Why this must be done carefully

`app/src/main/assets/database/e_katki_maddeleri_sade.sqlite` is a binary SQLite file. Do not update it with a text-only file writer. If the Room entity is changed before the asset schema is changed, the app may fail on startup with a schema mismatch.

## Safe local workflow

Run from the repository root:

```bash
python tools/add_english_columns_to_asset.py
```

This adds the following nullable columns if they do not already exist:

```sql
name_en TEXT
functional_class_en TEXT
health_status_en TEXT
description_en TEXT
warning_en TEXT
```

The script is idempotent and does not translate content. Empty English columns are expected at first.

## After the asset is updated

Then update `Additive.kt`:

```kotlin
val name_en: String?,
val functional_class_en: String?,
val health_status_en: String?,
val description_en: String?,
val warning_en: String?
```

Then update `AdditiveLocalization.kt` so English UI falls back safely:

```kotlin
fun Additive.localizedName(isEnglish: Boolean): String {
    return if (isEnglish) name_en?.takeIf { it.isNotBlank() } ?: name_tr else name_tr
}
```

Repeat the same pattern for the other free-text fields.

## Room versioning

When the asset schema changes, increment the Room database version in `AppDatabase.kt`.

Because the database is an app-bundled read-only asset and does not contain user-generated data, the existing destructive migration fallback is acceptable. If user-generated data such as favorites or search history is added later, real migrations must be written instead.
