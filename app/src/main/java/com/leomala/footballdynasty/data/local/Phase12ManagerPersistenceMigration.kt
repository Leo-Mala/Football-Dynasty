package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Additive Marco B persistence. Existing V6 careers receive no synthetic manager-state rows;
 * callers must materialize proven legacy/current values before manager mutations can execute.
 */
object Phase12ManagerPersistenceMigration {
    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_player_commercial` (
                    `careerId` TEXT NOT NULL,
                    `playerId` TEXT NOT NULL,
                    `salario` INTEGER NOT NULL,
                    `rcClause` INTEGER NOT NULL,
                    `rcRenewYear` INTEGER NOT NULL,
                    `rcConvYear` INTEGER NOT NULL,
                    `pendSaleClub` INTEGER NOT NULL,
                    `pendSaleValue` INTEGER NOT NULL,
                    `pendIsLoan` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `playerId`),
                    FOREIGN KEY(`careerId`, `playerId`) REFERENCES `career_player_runtime`(`careerId`, `playerId`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_player_commercial_playerId` " +
                    "ON `career_player_commercial` (`playerId`)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_player_transfer_state` (
                    `careerId` TEXT NOT NULL,
                    `playerId` TEXT NOT NULL,
                    `legacyPlayerCode` INTEGER NOT NULL,
                    `legacyClubCode` INTEGER NOT NULL,
                    `rawCrossActiveFlag` INTEGER NOT NULL,
                    `rawOCode` INTEGER NOT NULL,
                    `rawDCode` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `playerId`),
                    FOREIGN KEY(`careerId`, `playerId`) REFERENCES `career_player_runtime`(`careerId`, `playerId`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_player_transfer_state_playerId` " +
                    "ON `career_player_transfer_state` (`playerId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_career_player_transfer_state_careerId_legacyPlayerCode` " +
                    "ON `career_player_transfer_state` (`careerId`, `legacyPlayerCode`)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_club_manager_runtime` (
                    `careerId` TEXT NOT NULL,
                    `clubId` TEXT NOT NULL,
                    `active` INTEGER NOT NULL,
                    `cash` INTEGER NOT NULL,
                    `primarySlotPlayerCode` INTEGER,
                    `secondarySlotPlayerCode` INTEGER,
                    `rawStateFlag` INTEGER NOT NULL,
                    `ticketIncome` INTEGER NOT NULL,
                    `playerSaleIncome` INTEGER NOT NULL,
                    `prizeIncome` INTEGER NOT NULL,
                    `sponsorIncome` INTEGER NOT NULL,
                    `playerPurchaseExpense` INTEGER NOT NULL,
                    `stadiumExpense` INTEGER NOT NULL,
                    `salaryExpense` INTEGER NOT NULL,
                    `borrowingChargeExpense` INTEGER NOT NULL,
                    `fineExpense` INTEGER NOT NULL,
                    `miscellaneousExpense` INTEGER NOT NULL,
                    `borrowed` INTEGER NOT NULL,
                    `monthlyBorrowingCharge` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `clubId`),
                    FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`clubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_club_manager_runtime_clubId` " +
                    "ON `career_club_manager_runtime` (`clubId`)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_active_loans` (
                    `careerId` TEXT NOT NULL,
                    `playerId` TEXT NOT NULL,
                    `sourceClubId` TEXT NOT NULL,
                    `destinationClubId` TEXT NOT NULL,
                    `expiresAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `playerId`),
                    FOREIGN KEY(`careerId`, `playerId`) REFERENCES `career_player_runtime`(`careerId`, `playerId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`sourceClubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
                    FOREIGN KEY(`destinationClubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_active_loans_sourceClubId` " +
                    "ON `career_active_loans` (`sourceClubId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_active_loans_destinationClubId` " +
                    "ON `career_active_loans` (`destinationClubId`)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_stadium_constructions` (
                    `careerId` TEXT NOT NULL,
                    `sourceOrdinal` INTEGER NOT NULL,
                    `stadiumCode` INTEGER NOT NULL,
                    `endTimestampMillis` INTEGER NOT NULL,
                    `addition0` INTEGER NOT NULL,
                    `addition1` INTEGER NOT NULL,
                    `addition2` INTEGER NOT NULL,
                    `addition3` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `sourceOrdinal`),
                    FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
        }
    }
}
