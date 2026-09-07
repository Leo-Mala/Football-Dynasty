package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * V16 adds the first proven durable owner for legacy competition player rating aggregates
 * (`best.k0.g` / `components.n1`). Existing V15 careers have no provable historical aggregate,
 * therefore the new table is intentionally created empty: no fabricated backfill/default rows.
 */
object Phase16CompetitionPlayerRatingMigration {
    val MIGRATION_15_16: Migration = object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `career_competition_player_ratings` (`careerId` TEXT NOT NULL, `competitionId` TEXT NOT NULL, `playerId` TEXT NOT NULL, `legacyRatingSum` REAL NOT NULL, `legacyRatingCount` REAL NOT NULL, `legacyAverageRating` REAL NOT NULL, `legacyCategory` INTEGER NOT NULL, PRIMARY KEY(`careerId`, `competitionId`, `playerId`), FOREIGN KEY(`careerId`, `competitionId`) REFERENCES `career_competitions`(`careerId`, `competitionId`) ON UPDATE NO ACTION ON DELETE CASCADE)"""
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competition_player_ratings_careerId_competitionId` ON `career_competition_player_ratings` (`careerId`, `competitionId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_competition_player_ratings_careerId_playerId` ON `career_competition_player_ratings` (`careerId`, `playerId`)"
            )
        }
    }
}
