package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Additive, fail-closed storage for ticket inputs proven after Room V8 certification. */
object Phase13TicketRuntimeMigration {
    val MIGRATION_8_9: Migration = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_club_ticket_runtime` (
                    `careerId` TEXT NOT NULL,
                    `clubId` TEXT NOT NULL,
                    `rawDivisionCode` INTEGER NOT NULL,
                    `legacyManagerId` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `clubId`),
                    FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`clubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_club_ticket_runtime_clubId` " +
                    "ON `career_club_ticket_runtime` (`clubId`)"
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_manager_ticket_runtime` (
                    `careerId` TEXT NOT NULL,
                    `sourceOrdinal` INTEGER NOT NULL,
                    `legacyManagerId` INTEGER NOT NULL,
                    `rawH` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `sourceOrdinal`),
                    FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_manager_ticket_runtime_careerId_legacyManagerId` " +
                    "ON `career_manager_ticket_runtime` (`careerId`, `legacyManagerId`)"
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_match_construction_source` (
                    `careerId` TEXT NOT NULL,
                    `matchId` TEXT NOT NULL,
                    `sourceCode` TEXT NOT NULL,
                    PRIMARY KEY(`careerId`, `matchId`),
                    FOREIGN KEY(`careerId`, `matchId`) REFERENCES `career_scheduled_matches`(`careerId`, `matchId`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_match_construction_source_matchId` " +
                    "ON `career_match_construction_source` (`matchId`)"
            )
        }
    }
}
