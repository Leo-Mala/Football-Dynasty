package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * V19 adds the durable per-player/per-competition `best.r` owner needed by lineup eligibility.
 *
 * V18 never persisted these counters. The migration therefore creates an empty table and performs
 * no fabricated backfill. Callers must keep migrated rows fail-closed until exact state is
 * materialized from a proven source or from a V19+ lifecycle that started from proven zero state.
 */
object Phase17CompetitionDisciplineMigration {
    val MIGRATION_18_19: Migration = object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `career_player_competition_discipline` (`careerId` TEXT NOT NULL, `playerId` TEXT NOT NULL, `competitionId` TEXT NOT NULL, `legacyThreshold3Counter` INTEGER NOT NULL, `legacyThreshold1Counter` INTEGER NOT NULL, PRIMARY KEY(`careerId`, `playerId`, `competitionId`), FOREIGN KEY(`careerId`, `playerId`) REFERENCES `career_player_runtime`(`careerId`, `playerId`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`careerId`, `competitionId`) REFERENCES `career_competitions`(`careerId`, `competitionId`) ON UPDATE NO ACTION ON DELETE CASCADE)"""
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_player_competition_discipline_careerId_playerId` " +
                    "ON `career_player_competition_discipline` (`careerId`, `playerId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_player_competition_discipline_careerId_competitionId` " +
                    "ON `career_player_competition_discipline` (`careerId`, `competitionId`)"
            )
        }
    }
}
