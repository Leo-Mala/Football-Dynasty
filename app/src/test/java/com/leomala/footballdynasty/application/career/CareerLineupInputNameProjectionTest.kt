package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerLineupInputNameProjectionTest {
    @Test
    fun `lineup input names come from the matching persisted canonical and procedural identities`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A)))
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )
            database.playerDao().upsertAll(
                listOf(
                    PlayerEntity(
                        id = PLAYER_CANONICAL,
                        dataVersion = 1,
                        importScope = null,
                        name = "Canonical Name",
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
                        legacyHash = PLAYER_CANONICAL.hashCode(),
                    )
                )
            )

            val runtimeStore = CareerPlayerRuntimeStore(database, clockMillis = { 100L })
            runtimeStore.saveCanonicalRuntimeAndMembership(
                runtime = runtime(
                    playerId = PLAYER_CANONICAL,
                    sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
                ),
                membership = membership(PLAYER_CANONICAL, 0),
            )
            runtimeStore.saveProceduralPlayer(
                runtime = runtime(
                    playerId = PLAYER_PROCEDURAL,
                    sourceType = CareerPlayerRuntimeStore.SOURCE_PROCEDURAL,
                ),
                procedural = CareerProceduralPlayerEntity(
                    careerId = CAREER_A,
                    playerId = PLAYER_PROCEDURAL,
                    name = "Procedural Name",
                    country = 11,
                    position = 3,
                    status = 0,
                    side = 1,
                    cr1 = 11,
                    cr2 = 0,
                ),
                membership = membership(PLAYER_PROCEDURAL, 1),
            )

            val inputs = CareerLineupInputCatalogStore(database)
                .loadManagedClubLineupInputs(CAREER_A)

            assertEquals(
                listOf("Canonical Name", "Procedural Name"),
                inputs?.players?.map { it.name },
            )
            assertEquals(
                listOf(PLAYER_CANONICAL, PLAYER_PROCEDURAL),
                inputs?.players?.map { it.playerId },
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

    private fun membership(playerId: String, sourceOrdinal: Int) = CareerSquadMembershipEntity(
        careerId = CAREER_A,
        playerId = playerId,
        clubId = CLUB_A,
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
        const val PLAYER_CANONICAL = "player-canonical"
        const val PLAYER_PROCEDURAL = "player-procedural"
    }
}
