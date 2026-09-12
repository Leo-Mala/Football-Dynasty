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
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerTransferSearchCatalogStoreTest {
    @Test
    fun `catalog includes only persisted seniors from clubs other than managed club`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club(CLUB_A, "Managed Club"),
                    club(CLUB_B, "Other Club"),
                )
            )
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )
            val runtimeStore = CareerPlayerRuntimeStore(database, clockMillis = { 100L })
            runtimeStore.saveProceduralPlayer(
                runtime = runtime(PLAYER_MANAGED, 25, 77, 7_700),
                procedural = procedural(PLAYER_MANAGED, "Managed Player", 2),
                membership = membership(PLAYER_MANAGED, CLUB_A, "SENIOR", 0),
            )
            runtimeStore.saveProceduralPlayer(
                runtime = runtime(PLAYER_OTHER, 22, 81, 8_100),
                procedural = procedural(PLAYER_OTHER, "Other Senior", 3),
                membership = membership(PLAYER_OTHER, CLUB_B, "SENIOR", 4),
            )
            runtimeStore.saveProceduralPlayer(
                runtime = runtime(PLAYER_JUNIOR, 17, 65, 2_100),
                procedural = procedural(PLAYER_JUNIOR, "Other Junior", 1),
                membership = membership(PLAYER_JUNIOR, CLUB_B, "JUNIOR", 0),
            )

            val catalog = CareerTransferSearchCatalogStore(database).loadOtherSeniorPlayers(CAREER_A)

            assertEquals(CAREER_A, catalog?.careerId)
            assertEquals(CLUB_A, catalog?.managedClubId)
            assertEquals(listOf(PLAYER_OTHER), catalog?.rows?.map { it.playerId })
            val row = catalog?.rows?.single()
            assertEquals("Other Senior", row?.name)
            assertEquals(CLUB_B, row?.clubId)
            assertEquals("Other Club", row?.clubName)
            assertEquals(3, row?.position)
            assertEquals(22, row?.age)
            assertEquals(81, row?.overall)
            assertEquals(8_100, row?.marketValue)
            assertEquals(4, row?.sourceOrdinal)
        } finally {
            database.close()
        }
    }

    private fun runtime(
        playerId: String,
        age: Int,
        overall: Int,
        marketValue: Int,
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
        contractEndEpochMillis = 0L,
        legacyPreviousMarketValue = marketValue,
        legacyQ = false,
        legacyX = false,
        legacyY = false,
        legacyZ = false,
        legacyAnnualM = false,
        legacyAnnualN = 0.0,
        legacyRawPayrollN = 0,
        energy = 100,
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

    private fun membership(
        playerId: String,
        clubId: String,
        rosterKind: String,
        sourceOrdinal: Int,
    ) = CareerSquadMembershipEntity(
        careerId = CAREER_A,
        playerId = playerId,
        clubId = clubId,
        rosterKind = rosterKind,
        sourceOrdinal = sourceOrdinal,
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
        const val PLAYER_MANAGED = "player-managed"
        const val PLAYER_OTHER = "player-other"
        const val PLAYER_JUNIOR = "player-junior"
    }
}
