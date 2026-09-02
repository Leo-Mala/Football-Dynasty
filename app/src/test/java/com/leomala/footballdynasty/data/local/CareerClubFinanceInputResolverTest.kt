package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerCommercialEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.model.RosterKind
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerClubFinanceInputResolverTest {
    @Test
    fun `payroll resolves senior and junior salaries from persisted roster state`() = runBlocking {
        val database = database()
        try {
            seedBase(database)
            seedPlayer(database, "senior-a", RosterKind.SENIOR, 300, 0)
            seedPlayer(database, "junior-a", RosterKind.JUNIOR, 125, 0)
            seedPlayer(database, "senior-b", RosterKind.SENIOR, 200, 1)

            val (senior, junior) = CareerClubFinanceInputResolver(database).resolvePayroll(CAREER, CLUB)

            assertEquals(listOf(300, 200), senior)
            assertEquals(listOf(125), junior)
        } finally {
            database.close()
        }
    }

    @Test
    fun `payroll fails closed when a roster member has no commercial salary state`() = runBlocking {
        val database = database()
        try {
            seedBase(database)
            database.careerPlayerRuntimeDao().upsertRuntime(runtime("missing"))
            database.careerPlayerRuntimeDao().upsertMembership(
                CareerSquadMembershipEntity(CAREER, "missing", CLUB, RosterKind.SENIOR.name, 0)
            )

            val error = runCatching {
                CareerClubFinanceInputResolver(database).resolvePayroll(CAREER, CLUB)
            }.exceptionOrNull()

            assertNotNull(error)
        } finally {
            database.close()
        }
    }

    private suspend fun seedBase(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club()))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(CAREER, 1, "Finance resolver", null, null, 1L, 1L)
        )
    }

    private suspend fun seedPlayer(
        database: FootballDynastyDatabase,
        playerId: String,
        rosterKind: RosterKind,
        salary: Int,
        ordinal: Int,
    ) {
        database.careerPlayerRuntimeDao().upsertRuntime(runtime(playerId))
        database.careerPlayerRuntimeDao().upsertMembership(
            CareerSquadMembershipEntity(CAREER, playerId, CLUB, rosterKind.name, ordinal)
        )
        database.careerManagerRuntimeDao().upsertPlayerCommercial(
            CareerPlayerCommercialEntity(
                careerId = CAREER,
                playerId = playerId,
                salario = salary,
                rcClause = 0,
                rcRenewYear = 0,
                rcConvYear = 0,
                pendSaleClub = 0,
                pendSaleValue = 0,
                pendIsLoan = false,
            )
        )
    }

    private fun runtime(playerId: String) = CareerPlayerRuntimeEntity(
        careerId = CAREER,
        playerId = playerId,
        sourceType = "TEST",
        stateVersion = 1,
        age = 20,
        overall = 50,
        marketValue = 0,
        star = false,
        worldTop = false,
        legacyHash = 0,
        legacyGeneratedO = 0,
        legacyCreatedYear = 2026,
        contractEndEpochMillis = 0L,
        legacyPreviousMarketValue = 0,
        legacyQ = false,
        legacyX = false,
        legacyY = false,
        legacyZ = false,
    )

    private fun club() = ClubEntity(
        id = CLUB,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = CLUB,
        name = CLUB,
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
        legacyId = 303,
        legacyValid = true,
    )

    private fun database(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private companion object {
        const val CAREER = "career-finance-resolver"
        const val CLUB = "club-finance-resolver"
    }
}
