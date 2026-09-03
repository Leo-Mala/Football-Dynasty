package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Additive, fail-closed storage for legacy four-sector stadium capacities. */
object Phase13StadiumRuntimeMigration {
    val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `career_stadium_runtime` (
                    `careerId` TEXT NOT NULL,
                    `clubId` TEXT NOT NULL,
                    `sector0Capacity` INTEGER NOT NULL,
                    `sector1Capacity` INTEGER NOT NULL,
                    `sector2Capacity` INTEGER NOT NULL,
                    `sector3Capacity` INTEGER NOT NULL,
                    PRIMARY KEY(`careerId`, `clubId`),
                    FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`clubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_career_stadium_runtime_clubId` " +
                    "ON `career_stadium_runtime` (`clubId`)"
            )
        }
    }
}
