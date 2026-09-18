package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerActiveLoanEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerActiveLoanCatalogStoreTest {
    @Test
    fun `managed club loans resolve persisted identities and exclude unrelated loans`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club(CLUB_A, "Club A"),
                    club(CLUB_B, "Club B"),
                    club(CLUB_C, "Club C"),
                )
            )
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )
            database.playerDao().upsertAll(
                listOf(
                    player(PLAYER_OUT, "Canonical Out"),
                    player(PLAYER_UNRELATED, "Unrelated"),
                )
            )
            database.careerPlayerRuntimeDao().upsertRuntime(
                listOf(
                    runtime(PLAYER_OUT, CareerPlayerRuntimeStore.SOURCE_CANONICAL),
                    runtime(PLAYER_IN, CareerPlayerRuntimeStore.SOURCE_PROCEDURAL),
                    runtime(PLAYER_UNRELATED, CareerPlayerRuntimeStore.SOURCE_CANONICAL),
                )
            )
            database.careerPlayerRuntimeDao().upsertProceduralPlayer(
                CareerProceduralPlayerEntity(
                    careerId = CAREER_A,
                    playerId = PLAYER_IN,
                    name = "Procedural In",
                    country = 11,
                    position = 3,
                    status = 0,
                    side = 1,
                    cr1 = 11,
                    cr2 = 0,
                )
            )
            database.careerManagerRuntimeDao().upsertActiveLoan(
                CareerActiveLoanEntity(
                    careerId = CAREER_A,
                    playerId = PLAYER_OUT,
                    sourceClubId = CLUB_A,
                    destinationClubId = CLUB_B,
                    expiresAtEpochMillis = 2_000L,
                )
            )
            database.careerManagerRuntimeDao().upsertActiveLoan(
                CareerActiveLoanEntity(
                    careerId = CAREER_A,
                    playerId = PLAYER_IN,
                    sourceClubId = CLUB_C,
                    destinationClubId = CLUB_A,
                    expiresAtEpochMillis = 3_000L,
                )
            )
            database.careerManagerRuntimeDao().upsertActiveLoan(
                CareerActiveLoanEntity(
                    careerId = CAREER_A,
                    playerId = PLAYER_UNRELATED,
                    sourceClubId = CLUB_B,
                    destinationClubId = CLUB_C,
                    expiresAtEpochMillis = 4_000L,
                )
            )

            val snapshot = CareerActiveLoanCatalogStore(database)
                .loadManagedClubActiveLoans(CAREER_A)

            requireNotNull(snapshot)
            assertEquals(CAREER_A, snapshot.careerId)
            assertEquals(CLUB_A, snapshot.managedClubId)
            assertEquals(listOf(PLAYER_IN, PLAYER_OUT), snapshot.loans.map { it.playerId })

            val incoming = snapshot.loans[0]
            assertEquals("Procedural In", incoming.playerName)
            assertEquals("Club C", incoming.sourceClubName)
            assertEquals("Club A", incoming.destinationClubName)
            assertEquals(3_000L, incoming.expiresAtEpochMillis)
            assertFalse(incoming.managedClubIsSource)
            assertTrue(incoming.managedClubIsDestination)

            val outgoing = snapshot.loans[1]
            assertEquals("Canonical Out", outgoing.playerName)
            assertEquals("Club A", outgoing.sourceClubName)
            assertEquals("Club B", outgoing.destinationClubName)
            assertEquals(2_000L, outgoing.expiresAtEpochMillis)
            assertTrue(outgoing.managedClubIsSource)
            assertFalse(outgoing.managedClubIsDestination)
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing career fails closed`() = runBlocking {
        val database = database()
        try {
            assertNull(
                CareerActiveLoanCatalogStore(database)
                    .loadManagedClubActiveLoans("missing-career")
            )
        } finally {
            database.close()
        }
    }

    private fun runtime(playerId: String, sourceType: String) = CareerPlayerRuntimeEntity(
        careerId = CAREER_A,
        playerId = playerId,
        sourceType = sourceType,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 24,
        overall = 75,
        marketValue = 7_100,
        star = false,
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
        energy = 80,
        injuryUntilEpochDay = 0L,
    )

    private fun player(id: String, name: String) = PlayerEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        name = name,
        age = 25,
        country = 11,
        position = 1,
        status = 0,
        side = -1,
        cr1 = 13,
        cr2 = 0,
        star = false,
        worldTop = false,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyHash = id.hashCode(),
    )

    private fun club(id: String, name: String) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "teams/$id.ban",
        name = name,
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
        const val CLUB_C = "club-c"
        const val PLAYER_IN = "a-incoming-procedural"
        const val PLAYER_OUT = "b-outgoing-canonical"
        const val PLAYER_UNRELATED = "z-unrelated"
    }
}
