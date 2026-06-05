#!/usr/bin/env python3
"""
Ensures that the bundled Room SQLite asset contains every optional column
expected by the Additive entity.

Run from the repository root:

    python tools/add_english_columns_to_asset.py

The script is idempotent: existing columns are not added again.
It does not translate content. It only prepares the SQLite schema used by Room.
"""

from __future__ import annotations

import sqlite3
from pathlib import Path

DATABASE_PATH = Path("app/src/main/assets/database/e_katki_maddeleri_sade.sqlite")
TABLE_NAME = "additives"

COLUMNS_TO_ADD = {
    "name_en": "TEXT",
    "functional_class_en": "TEXT",
    "health_status_en": "TEXT",
    "description_en": "TEXT",
    "warning_en": "TEXT",
    "aliases_tr": "TEXT",
    "aliases_en": "TEXT",
    "updated_at": "TEXT",
}


def existing_columns(connection: sqlite3.Connection) -> set[str]:
    rows = connection.execute(f"PRAGMA table_info({TABLE_NAME})").fetchall()
    return {row[1] for row in rows}


def main() -> None:
    if not DATABASE_PATH.exists():
        raise FileNotFoundError(f"SQLite asset not found: {DATABASE_PATH}")

    with sqlite3.connect(DATABASE_PATH) as connection:
        columns = existing_columns(connection)
        missing = {
            name: column_type
            for name, column_type in COLUMNS_TO_ADD.items()
            if name not in columns
        }

        if not missing:
            print("All optional Additive columns already exist.")
            return

        for column_name, column_type in missing.items():
            print(f"Adding column: {column_name} {column_type}")
            connection.execute(
                f"ALTER TABLE {TABLE_NAME} ADD COLUMN {column_name} {column_type}"
            )

        connection.commit()

    print("SQLite asset schema updated successfully.")
    print("Next step: clean install the app and verify Room opens the bundled asset.")


if __name__ == "__main__":
    main()
