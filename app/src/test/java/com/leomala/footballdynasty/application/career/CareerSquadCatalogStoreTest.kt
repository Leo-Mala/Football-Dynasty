package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerSquadCatalogStoreTest {
    @Test
    fun `senior squad follows persisted membership order and managed club`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A), club(CLUB_B)))
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )
            val runtimeStore = CareerPlayerRuntimeStore(database, clockMillis = { 100L })
            runtimeStore.saveProceduralPlayer(
                runtime = runtime(PLAYER_A2, 24, 71, 7_100, 2_222L, 63),
                procedural = procedural(PLAYER_A2, "Player A2", 2),
                membership = membership(PLAYER_A2, CLUB_A, 1),
            )
            runtimeStore.saveProceduralPlayer(
                runtime = runtime(PLAYER_A1, 21, 74, 7_400, 1_111L, 88),
                procedural = procedural(PLAYER_A1, "Player A1", 1),
                membership = membership(PLAYER_A1, CLUB_A, 0),
            )
            runtimeStore.saveProceduralPlayer(
                runtime = runtime(PLAYER_B, 30, 80, 8_000, 9_999L, 42),
                procedural = procedural(PLAYER_B, "Player B", 3),
                membership = membership(PLAYER_B, CLUB_B, 0),
            )

            val squad = CareerSquadCatalogStore(database).loadSeniorSquad(CAREER_A)

            assertEquals(CLUB_A, squad?.clubId)
            assertEquals(listOf(PLAYER_A1, PLAYER_A2), squad?.players?.map { it.playerId })
            assertEquals(listOf(0, 1), squad?.players?.map { it.sourceOrdinal })
            assertEquals(listOf("Player A1", "Player A2"), squad?.players?.map { it.name })
            assertEquals(listOf(74, 71), squad?.players?.map { it.overall })
            assertEquals(listOf(7_400, 7_100), squad?.players?.map { it.marketValue })
            assertEquals(listOf(88, 63), squad?.players?.map { it.energy })
            assertEquals(listOf(1_111L, 2_222L), squad?.players?.map { it.contractEndEpochMillis })
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing core state fails closed instead of synthesizing a squad`() = runBlocking {
        val database = database()
        try {
            database.careerMetadataDao().upsert(
                CareerMetadataEntity(
                    id = CAREER_A,
                    dataVersion = 1,
                    displayName = "Incomplete",
                    legacyMetadataFingerprint = null,
                    legacyCareerFingerprint = null,
                    createdAtEpochMillis = 1L,
                    updatedAtEpochMillis = 1L,
                )
            )

            assertNull(CareerSquadCatalogStore(database).loadSeniorSquad(CAREER_A))
            assertNull(CareerSquadCatalogStore(database).loadSeniorSquad("missing"))
        } finally {
            database.close()
        }
    }

    private fun runtime(
        playerId: String,
        age: Int,
        overall: Int,
        marketValue: Int,
        contractEndEpochMillis: Long,
        energy: Int,
    ) = CareerPlayerRuntimeEntity(
        careerId = CAREER_A,
        playerId = playerId,
        sourceType = CareerPlayerRuntimeStore.SOURCE_PROCEDURAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = age,
        overall = overall,
        marketValue = marketValue,
        star = false,
        worldTop = false,
        legacyHash = playerId.hashCode(),
        legacyGeneratedO = 0,
        legacyCreatedYear = 2026,
        contractEndEpochMillis = contractEndEpochMillis,
        legacyPreviousMarketValue = marketValue,
        legacyQ = false,
        legacyX = false,
        legacyY = false,
        legacyZ = false,
        legacyAnnualM = false,
        legacyAnnualN = 0.0,
        legacyRawPayrollN = 0,
        energy = energy,
        injuryUntilEpochDay = 0L,
    )

    private fun procedural(playerId: String, name: String, position: Int) = CareerProceduralPlayerEntity(
        careerId = CAREER_A,
        playerId = playerId,
        name = name,
        country = 11,
        position = position,
        status = 0,
        side = 0,
        cr1 = 0,
        cr2 = 0,
    )

    private fun membership(playerId: String, clubId: String, sourceOrdinal: Int) =
        CareerSquadMembershipEntity(
            careerId = CAREER_A,
            playerId = playerId,
            clubId = clubId,
            rosterKind = "SENIOR",
            sourceOrdinal = sourceOrdinal,
        )

    private fun club(id: String) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "teams/$id.ban",
        name = id,
        country = 11,
        state = 0,
        level = 1,
        stadium = "",
        capacity = 0,
        reputation = 0,
        primaryColor = "",
        secondaryColor = "",
        coach = "",
        coachCountry = 0,
        baseColor = 0,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyVid = 0,
        legacyId = 0,
        legacyValid = true,
    )

    private fun database(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private companion object {
        const val CAREER_A = "career-a"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
        const val PLAYER_A1 = "player-a1"
        const val PLAYER_A2 = "player-a2"
        const val PLAYER_B = "player-b"
    }
}
