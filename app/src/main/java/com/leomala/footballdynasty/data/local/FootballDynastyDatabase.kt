package com.leomala.footballdynasty.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.leomala.footballdynasty.data.local.dao.CareerCompetitionDao
import com.leomala.footballdynasty.data.local.dao.CareerCoreStateDao
import com.leomala.footballdynasty.data.local.dao.CareerMetadataDao
import com.leomala.footballdynasty.data.local.dao.CareerPlayerRuntimeDao
import com.leomala.footballdynasty.data.local.dao.CareerScheduledMatchDao
import com.leomala.footballdynasty.data.local.dao.ClubDao
import com.leomala.footballdynasty.data.local.dao.LegacyImportDao
import com.leomala.footballdynasty.data.local.dao.PlayerDao
import com.leomala.footballdynasty.data.local.dao.SquadMembershipDao
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionMatchEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionStandingEntity
import com.leomala.footballdynasty.data.local.entity.CareerCoreStateEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerClubSeasonStatEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
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
        CareerPlayerRuntimeEntity::class,
        CareerProceduralPlayerEntity::class,
        CareerSquadMembershipEntity::class,
        CareerScheduledMatchEntity::class,
        CareerPlayerClubSeasonStatEntity::class,
        CareerCompetitionEntity::class,
        CareerCompetitionStandingEntity::class,
        CareerCompetitionMatchEntity::class,
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
    abstract fun careerPlayerRuntimeDao(): CareerPlayerRuntimeDao
    abstract fun careerScheduledMatchDao(): CareerScheduledMatchDao
    abstract fun careerCompetitionDao(): CareerCompetitionDao

    companion object {
        const val SCHEMA_VERSION: Int = 6
        const val DATABASE_NAME: String = "football_dynasty.db"
    }
}
