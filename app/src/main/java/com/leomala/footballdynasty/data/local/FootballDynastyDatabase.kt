package com.leomala.footballdynasty.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.leomala.footballdynasty.data.local.dao.CareerCoreStateDao
import com.leomala.footballdynasty.data.local.dao.CareerMetadataDao
import com.leomala.footballdynasty.data.local.dao.ClubDao
import com.leomala.footballdynasty.data.local.dao.LegacyImportDao
import com.leomala.footballdynasty.data.local.dao.PlayerDao
import com.leomala.footballdynasty.data.local.dao.SquadMembershipDao
import com.leomala.footballdynasty.data.local.entity.CareerCoreStateEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.local.entity.LegacyImportManifestEntity
import com.leomala.footballdynasty.data.local.entity.LegacyImportStateEntity
import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import com.leomala.footballdynasty.data.local.entity.SquadMembershipEntity

@Database(
    entities = [
        ClubEntity::class,
        PlayerEntity::class,
        SquadMembershipEntity::class,
        LegacyImportStateEntity::class,
        LegacyImportManifestEntity::class,
        CareerMetadataEntity::class,
        CareerCoreStateEntity::class,
    ],
    version = FootballDynastyDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
abstract class FootballDynastyDatabase : RoomDatabase() {
    abstract fun clubDao(): ClubDao
    abstract fun playerDao(): PlayerDao
    abstract fun squadMembershipDao(): SquadMembershipDao
    abstract fun legacyImportDao(): LegacyImportDao
    abstract fun careerMetadataDao(): CareerMetadataDao
    abstract fun careerCoreStateDao(): CareerCoreStateDao

    companion object {
        const val SCHEMA_VERSION: Int = 2
        const val DATABASE_NAME: String = "football_dynasty.db"
    }
}
