package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * V17 preserves only legacy state proven non-reconstructible after save/reopen.
 *
 * Every newly added scalar is nullable for V16 rows because no historical source exists there.
 * New history/snapshot tables are intentionally empty after migration: no fabricated backfill.
 */
object Phase17LegacyDurabilityMigration {
    val MIGRATION_16_17: Migration = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `career_competitions` ADD COLUMN `legacyCompetitionIndex` INTEGER")
            db.execSQL("ALTER TABLE `career_competitions` ADD COLUMN `legacyGroupCountA0` INTEGER")
            db.execSQL("ALTER TABLE `career_competition_player_ratings` ADD COLUMN `legacyStableOrdinal` INTEGER")
            db.execSQL("ALTER TABLE `career_scheduled_matches` ADD COLUMN `legacyDayMatchOrdinal` INTEGER")
            db.execSQL("ALTER TABLE `career_scheduled_matches` ADD COLUMN `legacyTieBreakActive` INTEGER")
            db.execSQL("ALTER TABLE `career_scheduled_matches` ADD COLUMN `legacyTieBreakWinnerClubId` TEXT")

            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `career_competition_snapshots` (`careerId` TEXT NOT NULL, `competitionId` TEXT NOT NULL, `snapshotKind` TEXT NOT NULL, `snapshotOrdinal` INTEGER NOT NULL, `legacyA` INTEGER NOT NULL, `legacyB` INTEGER NOT NULL, `topPlayerId` TEXT, PRIMARY KEY(`careerId`, `competitionId`, `snapshotKind`, `snapshotOrdinal`), FOREIGN KEY(`careerId`, `competitionId`) REFERENCES `career_competitions`(`careerId`, `competitionId`) ON UPDATE NO ACTION ON DELETE CASCADE)"""
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competition_snapshots_careerId_competitionId` ON `career_competition_snapshots` (`careerId`, `competitionId`)"
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `career_competition_snapshot_members` (`careerId` TEXT NOT NULL, `competitionId` TEXT NOT NULL, `snapshotKind` TEXT NOT NULL, `snapshotOrdinal` INTEGER NOT NULL, `memberOrdinal` INTEGER NOT NULL, `playerId` TEXT NOT NULL, `clubIdAtSnapshot` TEXT, PRIMARY KEY(`careerId`, `competitionId`, `snapshotKind`, `snapshotOrdinal`, `memberOrdinal`), FOREIGN KEY(`careerId`, `competitionId`, `snapshotKind`, `snapshotOrdinal`) REFERENCES `career_competition_snapshots`(`careerId`, `competitionId`, `snapshotKind`, `snapshotOrdinal`) ON UPDATE NO ACTION ON DELETE CASCADE)"""
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competition_snapshot_members_careerId_competitionId_snapshotKind_snapshotOrdinal` ON `career_competition_snapshot_members` (`careerId`, `competitionId`, `snapshotKind`, `snapshotOrdinal`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competition_snapshot_members_careerId_playerId` ON `career_competition_snapshot_members` (`careerId`, `playerId`)"
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `career_player_match_rating_history` (`careerId` TEXT NOT NULL, `playerId` TEXT NOT NULL, `legacyDayIndexB` INTEGER NOT NULL, `legacyDayMatchIndexC` INTEGER NOT NULL, `legacyRating` REAL NOT NULL, PRIMARY KEY(`careerId`, `playerId`, `legacyDayIndexB`, `legacyDayMatchIndexC`), FOREIGN KEY(`careerId`, `playerId`) REFERENCES `career_player_runtime`(`careerId`, `playerId`) ON UPDATE NO ACTION ON DELETE CASCADE)"""
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_player_match_rating_history_careerId_playerId` ON `career_player_match_rating_history` (`careerId`, `playerId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_player_match_rating_history_careerId_legacyDayIndexB_legacyDayMatchIndexC` ON `career_player_match_rating_history` (`careerId`, `legacyDayIndexB`, `legacyDayMatchIndexC`)"
            )
        }
    }
}
