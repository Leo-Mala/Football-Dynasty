package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the exact serialized `best.c0.S/T` tactics slice to the career-local club runtime.
 *
 * V17 modern careers had no UI/runtime surface capable of mutating `DialogTatics.j()` state, while
 * every recovered 2026/27 `best.c0` constructor initializes `S={0,0,0,0}` and `T=false`.
 * Backfilling those constructor values is therefore evidence-backed compatibility, not a guessed
 * gameplay default.
 */
object Phase17ClubTacticsPersistenceMigration {
    val MIGRATION_17_18: Migration = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE `career_club_manager_runtime` " +
                    "ADD COLUMN `legacyTacticOption0` INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE `career_club_manager_runtime` " +
                    "ADD COLUMN `legacyTacticOption1` INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE `career_club_manager_runtime` " +
                    "ADD COLUMN `legacyTacticOption2` INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE `career_club_manager_runtime` " +
                    "ADD COLUMN `legacyTacticOption3` INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE `career_club_manager_runtime` " +
                    "ADD COLUMN `legacyTacticCheckboxT` INTEGER NOT NULL DEFAULT 0"
            )
        }
    }
}
