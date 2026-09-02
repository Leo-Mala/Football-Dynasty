package com.leomala.footballdynasty.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.leomala.footballdynasty.data.local.dao.CareerCoachRuntimeDao
import com.leomala.footballdynasty.data.local.dao.CareerCompetitionDao
import com.leomala.footballdynasty.data.local.dao.CareerCoreStateDao
import com.leomala.footballdynasty.data.local.dao.CareerManagerRuntimeDao
import com.leomala.footballdynasty.data.local.dao.CareerMetadataDao
import com.leomala.footballdynasty.data.local.dao.CareerPlayerRuntimeDao
import com.leomala.footballdynasty.data.local.dao.CareerScheduledMatchDao
import com.leomala.footballdynasty.data.local.dao.CareerTicketRuntimeDao
import com.leomala.footballdynasty.data.local.dao.ClubDao
import com.leomala.footballdynasty.data.local.dao.LegacyImportDao
import com.leomala.footballdynasty.data.local.dao.PlayerDao
import com.leomala.footballdynasty.data.local.dao.SquadMembershipDao
import com.leomala.footballdynasty.data.local.entity.*

@Database(
    entities = [
        ClubEntity::class, PlayerEntity::class, SquadMembershipEntity::class,
        LegacyImportStateEntity::class, LegacyImportManifestEntity::class,
        CareerMetadataEntity::class, CareerCoreStateEntity::class,
        CareerPlayerRuntimeEntity::class, CareerProceduralPlayerEntity::class,
        CareerSquadMembershipEntity::class, CareerScheduledMatchEntity::class,
        CareerPlayerClubSeasonStatEntity::class, CareerCompetitionEntity::class,
        CareerCompetitionStandingEntity::class, CareerCompetitionMatchEntity::class,
        CareerPlayerCommercialEntity::class, CareerPlayerTransferStateEntity::class,
        CareerClubManagerRuntimeEntity::class, CareerActiveLoanEntity::class,
        CareerStadiumConstructionEntity::class, CareerStadiumRuntimeEntity::class,
        CareerClubTicketRuntimeEntity::class, CareerManagerTicketRuntimeEntity::class,
        CareerMatchConstructionSourceEntity::class, CareerCoachRuntimeEntity::class,
        CareerCoachSeasonClubRecordEntity::class,
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
    abstract fun careerManagerRuntimeDao(): CareerManagerRuntimeDao
    abstract fun careerTicketRuntimeDao(): CareerTicketRuntimeDao
    abstract fun careerCoachRuntimeDao(): CareerCoachRuntimeDao

    companion object {
        const val SCHEMA_VERSION: Int = 11
        const val DATABASE_NAME: String = "football_dynasty.db"
    }
}
