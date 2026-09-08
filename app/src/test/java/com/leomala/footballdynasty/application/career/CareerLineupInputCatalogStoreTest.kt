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
class CareerLineupInputCatalogStoreTest {
    @Test
    fun `lineup inputs hydrate only persisted managed club senior players`() = runBlocking {
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
                runtime = runtime(PLAYER_A2, overall = 71, energy = 63, star = false),
                procedural = procedural(PLAYER_A2, position = 3, side = -1, cr1 = 11, cr2 = 0),
                membership = membership(PLAYER_A2, CLUB_A, 1),
            )
            runtimeStore.saveProceduralPlayer(
                runtime = runtime(PLAYER_A1, overall = 84, energy = 88, star = true),
                procedural = procedural(PLAYER_A1, position = 4, side = 1, cr1 = 8, cr2 = 0),
                membership = membership(PLAYER_A1, CLUB_A, 0),
            )
            runtimeStore.saveProceduralPlayer(
                runtime = runtime(PLAYER_B, overall = 99, energy = 42, star = true),
                procedural = procedural(PLAYER_B, position = 1, side = 0, cr1 = 13, cr2 = 0),
                membership = membership(PLAYER_B, CLUB_B, 0),
            )

            val inputs = CareerLineupInputCatalogStore(database)
                .loadManagedClubLineupInputs(CAREER_A)

            assertEquals(CAREER_A, inputs?.careerId)
            assertEquals(CLUB_A, inputs?.clubId)
            assertEquals(listOf(PLAYER_A1, PLAYER_A2), inputs?.players?.map { it.playerId })
            assertEquals(listOf(0, 1), inputs?.players?.map { it.sourceOrdinal })
            assertEquals(listOf(4, 3), inputs?.players?.map { it.positionCode })
            assertEquals(listOf(1, -1), inputs?.players?.map { it.sideCode })
            assertEquals(listOf(2, 1), inputs?.players?.map { it.subroleCode })
            assertEquals(listOf(84, 71), inputs?.players?.map { it.skill })
            assertEquals(listOf(88, 63), inputs?.players?.map { it.energy })
            assertEquals(listOf(true, false), inputs?.players?.map { it.star })
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing core state fails closed instead of synthesizing lineup inputs`() = runBlocking {
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

            val store = CareerLineupInputCatalogStore(database)
            assertNull(store.loadManagedClubLineupInputs(CAREER_A))
            assertNull(store.loadManagedClubLineupInputs("missing"))
        } finally {
            database.close()
        }
    }

    private fun runtime(
        playerId: String,
        overall: Int,
        energy: Int,
        star: Boolean,
    ) = CareerPlayerRuntimeEntity(
        careerId = CAREER_A,
        playerId = playerId,
        sourceType = CareerPlayerRuntimeStore.SOURCE_PROCEDURAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 24,
        overall = overall,
        marketValue = 7_100,
        star = star,
        worldTop = false,
        legacyHash = playerId.hashCode(),
        legacyGeneratedO = 0,
        legacyCreatedYear = 2026,
        contractEndEpochMillis = 2_222L,
        legacyPreviousMarketValue = 7_100,
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

    private fun procedural(
        playerId: String,
        position: Int,
        side: Int,
        cr1: Int,
        cr2: Int,
    ) = CareerProceduralPlayerEntity(
        careerId = CAREER_A,
        playerId = playerId,
        name = playerId,
        country = 11,
        position = position,
        status = 0,
        side = side,
        cr1 = cr1,
        cr2 = cr2,
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
