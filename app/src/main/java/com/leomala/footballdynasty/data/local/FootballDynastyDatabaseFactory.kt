package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object FootballDynastyMigrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `career_core_state` (`careerId` TEXT NOT NULL, `stateVersion` INTEGER NOT NULL, `seasonNumber` INTEGER NOT NULL, `seasonYear` INTEGER NOT NULL, `calendarYear` INTEGER NOT NULL, `currentDayIndex` INTEGER NOT NULL, `startDayIndex` INTEGER NOT NULL, `dayCount` INTEGER NOT NULL, `rngInitialSeed` INTEGER NOT NULL, `rngInternalState` INTEGER NOT NULL, `rngDraws` INTEGER NOT NULL, `managedClubId` TEXT, `transitionCount` INTEGER NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`careerId`), FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_core_state_managedClubId` ON `career_core_state` (`managedClubId`)")
        }
    }
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `career_player_runtime` (`careerId` TEXT NOT NULL, `playerId` TEXT NOT NULL, `sourceType` TEXT NOT NULL, `stateVersion` INTEGER NOT NULL, `age` INTEGER NOT NULL, `overall` INTEGER NOT NULL, `marketValue` INTEGER NOT NULL, `star` INTEGER NOT NULL, `worldTop` INTEGER NOT NULL, `legacyHash` INTEGER NOT NULL, `legacyGeneratedO` INTEGER NOT NULL, `legacyCreatedYear` INTEGER NOT NULL, `contractEndEpochMillis` INTEGER NOT NULL, `legacyPreviousMarketValue` INTEGER NOT NULL, `legacyQ` INTEGER NOT NULL, `legacyX` INTEGER NOT NULL, `legacyY` INTEGER NOT NULL, `legacyZ` INTEGER NOT NULL, PRIMARY KEY(`careerId`, `playerId`), FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_player_runtime_careerId` ON `career_player_runtime` (`careerId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_player_runtime_playerId` ON `career_player_runtime` (`playerId`)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `career_procedural_players` (`careerId` TEXT NOT NULL, `playerId` TEXT NOT NULL, `name` TEXT NOT NULL, `country` INTEGER NOT NULL, `position` INTEGER NOT NULL, `status` INTEGER NOT NULL, `side` INTEGER NOT NULL, `cr1` INTEGER NOT NULL, `cr2` INTEGER NOT NULL, PRIMARY KEY(`careerId`, `playerId`), FOREIGN KEY(`careerId`, `playerId`) REFERENCES `career_player_runtime`(`careerId`, `playerId`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_procedural_players_playerId` ON `career_procedural_players` (`playerId`)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `career_squad_memberships` (`careerId` TEXT NOT NULL, `playerId` TEXT NOT NULL, `clubId` TEXT NOT NULL, `rosterKind` TEXT NOT NULL, `sourceOrdinal` INTEGER NOT NULL, PRIMARY KEY(`careerId`, `playerId`), FOREIGN KEY(`careerId`, `playerId`) REFERENCES `career_player_runtime`(`careerId`, `playerId`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`clubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_squad_memberships_clubId` ON `career_squad_memberships` (`clubId`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_career_squad_memberships_careerId_clubId_rosterKind_sourceOrdinal` ON `career_squad_memberships` (`careerId`, `clubId`, `rosterKind`, `sourceOrdinal`)")
        }
    }
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `career_scheduled_matches` (`careerId` TEXT NOT NULL, `matchId` TEXT NOT NULL, `dayIndex` INTEGER NOT NULL, `eventTypeCode` INTEGER NOT NULL, `homeClubId` TEXT NOT NULL, `awayClubId` TEXT NOT NULL, `processed` INTEGER NOT NULL, `homeGoals` INTEGER, `awayGoals` INTEGER, PRIMARY KEY(`careerId`, `matchId`), FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`homeClubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION, FOREIGN KEY(`awayClubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_scheduled_matches_careerId_dayIndex` ON `career_scheduled_matches` (`careerId`, `dayIndex`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_scheduled_matches_homeClubId` ON `career_scheduled_matches` (`homeClubId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_scheduled_matches_awayClubId` ON `career_scheduled_matches` (`awayClubId`)")
        }
    }
    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `career_player_runtime` ADD COLUMN `energy` INTEGER NOT NULL DEFAULT 100")
            db.execSQL("ALTER TABLE `career_player_runtime` ADD COLUMN `injuryUntilEpochDay` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `career_player_club_season_stats` (`careerId` TEXT NOT NULL, `playerId` TEXT NOT NULL, `legacySeasonId` INTEGER NOT NULL, `legacyClubId` INTEGER NOT NULL, `legacyC` INTEGER NOT NULL, `legacyD` INTEGER NOT NULL, `legacyE` INTEGER NOT NULL, `legacyF` INTEGER NOT NULL, `legacyG` INTEGER NOT NULL, `legacyH` INTEGER NOT NULL, PRIMARY KEY(`careerId`, `playerId`, `legacySeasonId`, `legacyClubId`), FOREIGN KEY(`careerId`, `playerId`) REFERENCES `career_player_runtime`(`careerId`, `playerId`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_player_club_season_stats_careerId_playerId` ON `career_player_club_season_stats` (`careerId`, `playerId`)")
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
        Phase10CompetitionMigration.MIGRATION_5_6,
        Phase12ManagerPersistenceMigration.MIGRATION_6_7,
        Phase13StadiumRuntimeMigration.MIGRATION_7_8,
        Phase13TicketRuntimeMigration.MIGRATION_8_9,
        Phase13StadiumConstructionOwnershipMigration.MIGRATION_9_10,
        Phase14CoachRuntimeMigration.MIGRATION_10_11,
        Phase14CompetitionInputsMigration.MIGRATION_11_12,
        Phase14CompetitionInputsMigration.MIGRATION_12_13,
    )
}

object FootballDynastyDatabaseFactory {
    fun create(context: Context): FootballDynastyDatabase =
        Room.databaseBuilder(context.applicationContext, FootballDynastyDatabase::class.java, FootballDynastyDatabase.DATABASE_NAME)
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
}
