#!/usr/bin/env python3
"""
Verifies that the bundled SQLite asset matches the columns expected by
app/src/main/java/com/mehmetbukum/fooddetective/data/Additive.kt.

Run from the repository root before creating a release build:

    python tools/verify_asset_schema.py

This script does not modify the database. It only reports missing columns
or indexes.
"""

from __future__ import annotations

import sqlite3
import sys
from pathlib import Path

DATABASE_PATH = Path("app/src/main/assets/database/e_katki_maddeleri_sade.sqlite")
TABLE_NAME = "additives"

REQUIRED_COLUMNS = {
    "id",
    "code",
    "name_tr",
    "functional_class",
    "halal_status",
    "health_status",
    "risk_level",
    "description",
    "warning",
    "name_en",
    "functional_class_en",
    "health_status_en",
    "description_en",
    "warning_en",
    "aliases_tr",
    "aliases_en",
    "updated_at",
}

REQUIRED_UNIQUE_INDEXES = {
    "index_additives_code": ("code",),
}


def table_exists(connection: sqlite3.Connection, table_name: str) -> bool:
    row = connection.execute(
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
        (table_name,),
    ).fetchone()
    return row is not None


def existing_columns(connection: sqlite3.Connection) -> set[str]:
    rows = connection.execute(f"PRAGMA table_info({TABLE_NAME})").fetchall()
    return {row[1] for row in rows}


def existing_unique_indexes(connection: sqlite3.Connection) -> dict[str, tuple[str, ...]]:
    indexes: dict[str, tuple[str, ...]] = {}
    for row in connection.execute(f"PRAGMA index_list({TABLE_NAME})").fetchall():
        name = row[1]
        is_unique = bool(row[2])
        if not is_unique:
            continue

        index_columns = connection.execute(f"PRAGMA index_info({name})").fetchall()
        indexes[name] = tuple(column_row[2] for column_row in index_columns)

    return indexes


def main() -> int:
    if not DATABASE_PATH.exists():
        print(f"ERROR: SQLite asset not found: {DATABASE_PATH}")
        return 1

    with sqlite3.connect(DATABASE_PATH) as connection:
        if not table_exists(connection, TABLE_NAME):
            print(f"ERROR: Table not found: {TABLE_NAME}")
            return 1

        columns = existing_columns(connection)
        unique_indexes = existing_unique_indexes(connection)

    missing = sorted(REQUIRED_COLUMNS - columns)
    extra = sorted(columns - REQUIRED_COLUMNS)

    if missing:
        print("ERROR: Missing Additive columns in bundled asset:")
        for column in missing:
            print(f"  - {column}")
        print("\nRun: python tools/add_english_columns_to_asset.py")
        return 1

    missing_indexes = {
        name: columns
        for name, columns in REQUIRED_UNIQUE_INDEXES.items()
        if unique_indexes.get(name) != columns
    }

    if missing_indexes:
        print("ERROR: Missing unique Additive indexes in bundled asset:")
        for name, columns in missing_indexes.items():
            print(f"  - {name} on ({', '.join(columns)})")
        return 1

    print("OK: Bundled SQLite asset contains all Additive columns.")
    print(f"Columns checked: {len(REQUIRED_COLUMNS)}")
    print(f"Unique indexes checked: {len(REQUIRED_UNIQUE_INDEXES)}")

    if extra:
        print("Note: Extra columns are present but harmless:")
        for column in extra:
            print(f"  - {column}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
