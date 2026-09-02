package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Additive V10 -> V11 coach runtime. No source row is backfilled because V10 cannot prove the new
 * fields. Existing careers therefore retain certified V10 state and fail closed on Phase-14 coach
 * operations until the complete manager state is explicitly materialized.
 */
object Phase14CoachRuntimeMigration {
    val MIGRATION_10_11: Migration = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_coach_runtime` (
                    `careerId` TEXT NOT NULL,
                    `managerSourceOrdinal` INTEGER NOT NULL,
                    `isUserControlled` INTEGER NOT NULL,
                    `currentClubId` TEXT,
                    `alternativeClubId` TEXT,
                    `previousClubId` TEXT,
                    `previousClubCountry` INTEGER,
                    `previousClubDivisionIndex` INTEGER,
                    `rawG` INTEGER NOT NULL,
                    `rawD` INTEGER NOT NULL,
                    `rawE` INTEGER NOT NULL,
                    `rawF` INTEGER NOT NULL,
                    `rawO` INTEGER NOT NULL,
                    `rawM` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `managerSourceOrdinal`),
                    FOREIGN KEY(`careerId`, `managerSourceOrdinal`)
                        REFERENCES `career_manager_ticket_runtime`(`careerId`, `sourceOrdinal`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_coach_season_club_records` (
                    `careerId` TEXT NOT NULL,
                    `managerSourceOrdinal` INTEGER NOT NULL,
                    `sourceOrdinal` INTEGER NOT NULL,
                    `legacySeasonId` INTEGER NOT NULL,
                    `legacyClubId` INTEGER NOT NULL,
                    `rawMatches` INTEGER NOT NULL,
                    `rawWins` INTEGER NOT NULL,
                    `rawLosses` INTEGER NOT NULL,
                    `rawPoints` INTEGER NOT NULL,
                    `rawOtherCount` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `managerSourceOrdinal`, `sourceOrdinal`),
                    FOREIGN KEY(`careerId`, `managerSourceOrdinal`)
                        REFERENCES `career_coach_runtime`(`careerId`, `managerSourceOrdinal`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_coach_season_club_records_careerId_managerSourceOrdinal` " +
                    "ON `career_coach_season_club_records` (`careerId`, `managerSourceOrdinal`)"
            )
        }
    }
}
