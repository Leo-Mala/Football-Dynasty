package com.leomala.footballdynasty.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Phase 15 durable senior-runtime delta.
 *
 * `best.o.M`, `best.o.N` and raw payroll `best.o.n` are ordinary serialized fields in the official
 * legacy corpus. Existing V14 careers cannot have their historical values reconstructed safely, so
 * all three columns are added nullable and this migration deliberately performs no UPDATE/backfill.
 * Legacy `best.o.d` already has the proven modern owner `CareerPlayerRuntimeEntity.worldTop`.
 * Transient `best.o.j0` is intentionally not persisted.
 */
object Phase15SeniorRuntimeMigration {
    val MIGRATION_14_15: Migration = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `career_player_runtime` ADD COLUMN `legacyAnnualM` INTEGER")
            db.execSQL("ALTER TABLE `career_player_runtime` ADD COLUMN `legacyAnnualN` REAL")
            db.execSQL("ALTER TABLE `career_player_runtime` ADD COLUMN `legacyRawPayrollN` INTEGER")
        }
    }
}
