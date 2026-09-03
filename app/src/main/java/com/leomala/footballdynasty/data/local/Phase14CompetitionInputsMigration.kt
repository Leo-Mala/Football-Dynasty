package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Additive Phase 14 competition inputs. Previous schemas are never backfilled with guessed values. */
object Phase14CompetitionInputsMigration {
    val MIGRATION_11_12: Migration = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `career_competitions` ADD COLUMN `legacyRelegationCount` INTEGER"
            )
        }
    }

    /** Exact serialized `konrent.t.x0()`; V12 rows have no lossless source, therefore NULL. */
    val MIGRATION_12_13: Migration = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `career_competitions` ADD COLUMN `legacyLeagueSubtype` INTEGER"
            )
        }
    }
}
