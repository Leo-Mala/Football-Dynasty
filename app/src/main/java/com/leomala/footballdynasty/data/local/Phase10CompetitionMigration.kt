package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Additive V5 -> V6 migration for the proven serializable competition runtime. */
object Phase10CompetitionMigration {
    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_competitions` (
                    `careerId` TEXT NOT NULL,
                    `competitionId` TEXT NOT NULL,
                    `legacyCompetitionType` INTEGER NOT NULL,
                    `legacyFormatCode` INTEGER NOT NULL,
                    `currentRoundNumber` INTEGER NOT NULL,
                    `totalRounds` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `competitionId`),
                    FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competitions_careerId` " +
                    "ON `career_competitions` (`careerId`)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_competition_standings` (
                    `careerId` TEXT NOT NULL,
                    `competitionId` TEXT NOT NULL,
                    `clubId` TEXT NOT NULL,
                    `stableOrdinal` INTEGER NOT NULL,
                    `points` INTEGER NOT NULL,
                    `played` INTEGER NOT NULL,
                    `wins` INTEGER NOT NULL,
                    `losses` INTEGER NOT NULL,
                    `goalsFor` INTEGER NOT NULL,
                    `goalsAgainst` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `competitionId`, `clubId`),
                    FOREIGN KEY(`careerId`, `competitionId`) REFERENCES `career_competitions`(`careerId`, `competitionId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`clubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competition_standings_careerId_competitionId` " +
                    "ON `career_competition_standings` (`careerId`, `competitionId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competition_standings_clubId` " +
                    "ON `career_competition_standings` (`clubId`)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_competition_matches` (
                    `careerId` TEXT NOT NULL,
                    `competitionId` TEXT NOT NULL,
                    `matchId` TEXT NOT NULL,
                    `roundNumber` INTEGER NOT NULL,
                    `fixtureOrdinal` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `competitionId`, `matchId`),
                    FOREIGN KEY(`careerId`, `competitionId`) REFERENCES `career_competitions`(`careerId`, `competitionId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`careerId`, `matchId`) REFERENCES `career_scheduled_matches`(`careerId`, `matchId`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competition_matches_careerId_competitionId` " +
                    "ON `career_competition_matches` (`careerId`, `competitionId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competition_matches_careerId_matchId` " +
                    "ON `career_competition_matches` (`careerId`, `matchId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_career_competition_matches_careerId_competitionId_roundNumber_fixtureOrdinal` " +
                    "ON `career_competition_matches` (`careerId`, `competitionId`, `roundNumber`, `fixtureOrdinal`)"
            )
        }
    }
}
