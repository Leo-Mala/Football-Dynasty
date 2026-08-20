package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration

object FootballDynastyMigrations {
    /**
     * Schema V1 is the first modern database version, so there is no V0 -> V1
     * migration. Every future schema version must append an explicit migration
     * here; destructive fallback is intentionally forbidden.
     */
    val ALL: Array<Migration> = emptyArray()
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
