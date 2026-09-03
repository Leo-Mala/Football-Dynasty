package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Additive V9 -> V10 ownership seam for stadium constructions.
 *
 * Existing rows intentionally remain NULL: V9 has no proven owner mapping and migration must not
 * invent one. Only newly started constructions materialize the already-known modern club context.
 */
object Phase13StadiumConstructionOwnershipMigration {
    val MIGRATION_9_10: Migration = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE `career_stadium_constructions` ADD COLUMN `ownerClubId` TEXT"
            )
        }
    }
}
