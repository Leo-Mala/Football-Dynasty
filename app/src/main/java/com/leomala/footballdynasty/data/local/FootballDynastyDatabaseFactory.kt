package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object FootballDynastyMigrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_core_state` (
                    `careerId` TEXT NOT NULL,
                    `stateVersion` INTEGER NOT NULL,
                    `seasonNumber` INTEGER NOT NULL,
                    `seasonYear` INTEGER NOT NULL,
                    `calendarYear` INTEGER NOT NULL,
                    `currentDayIndex` INTEGER NOT NULL,
                    `startDayIndex` INTEGER NOT NULL,
                    `dayCount` INTEGER NOT NULL,
                    `rngInitialSeed` INTEGER NOT NULL,
                    `rngInternalState` INTEGER NOT NULL,
                    `rngDraws` INTEGER NOT NULL,
                    `managedClubId` TEXT,
                    `transitionCount` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`),
                    FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_core_state_managedClubId` " +
                    "ON `career_core_state` (`managedClubId`)"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}

object FootballDynastyDatabaseFactory {
    fun create(context: Context): FootballDynastyDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            FootballDynastyDatabase::class.java,
            FootballDynastyDatabase.DATABASE_NAME,
        )
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
}
