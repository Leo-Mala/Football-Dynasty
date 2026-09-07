package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Additive persistence boundary for the pre-promotion legacy `best.p` junior draft. */
object Phase15JuniorDraftMigration {
    val MIGRATION_13_14: Migration = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `career_junior_drafts` (`careerId` TEXT NOT NULL, `clubId` TEXT NOT NULL, `sourceOrdinal` INTEGER NOT NULL, `legacyN` INTEGER NOT NULL, `legacyB` INTEGER NOT NULL, `legacyC` INTEGER NOT NULL, `legacyE` INTEGER NOT NULL, `legacyJ` INTEGER NOT NULL, `legacyL` INTEGER NOT NULL, `legacyD` INTEGER NOT NULL, `name` TEXT NOT NULL, `legacyG` INTEGER NOT NULL, `legacyF` INTEGER NOT NULL, `legacyO` INTEGER NOT NULL, `legacyM` INTEGER NOT NULL, `legacyH` INTEGER NOT NULL, `legacyI` INTEGER NOT NULL, `developmentRemainder` REAL NOT NULL, PRIMARY KEY(`careerId`, `clubId`, `sourceOrdinal`), FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`clubId`) REFERENCES `clubs`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION)"""
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_junior_drafts_careerId` ON `career_junior_drafts` (`careerId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_career_junior_drafts_clubId` ON `career_junior_drafts` (`clubId`)")
        }
    }
}
