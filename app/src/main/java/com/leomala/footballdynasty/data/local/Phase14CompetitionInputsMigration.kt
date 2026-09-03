package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Additive V11 -> V12 migration for one exact `best.f0.i(best.s)` input.
 *
 * Existing V11 competition rows have no proven source for `LoadLigaOptions.nRebaixados`, so the
 * new column is deliberately nullable and receives no default/backfill. Runtime promotion must
 * continue to fail closed until a caller supplies the exact legacy value.
 */
object Phase14CompetitionInputsMigration {
    val MIGRATION_11_12: Migration = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `career_competitions` ADD COLUMN `legacyRelegationCount` INTEGER"
            )
        }
    }
}
