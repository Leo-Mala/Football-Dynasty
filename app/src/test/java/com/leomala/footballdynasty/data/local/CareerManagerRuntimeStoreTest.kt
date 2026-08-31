package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyPlayerCommercialState
import com.leomala.footballdynasty.domain.manager.LegacyStadiumExpansionRule
import com.leomala.footballdynasty.domain.manager.LegacyStadiumFinanceRuntimeRule
import com.leomala.footballdynasty.domain.manager.LegacyTransferClubRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionInput
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionRule
import com.leomala.footballdynasty.domain.manager.LegacyTransferPlayerRuntimeState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerManagerRuntimeStoreTest {
    @Test
    fun `purchase transfer commercial finance and membership survive database reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "marco-b-transfer-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        seed(database)
        var store = CareerManagerRuntimeStore(database)
        materializeTransferFixture(store)

        val plan = purchasePlan(destinationLegacyClubId = 202, transferValue = 1_000)
        val after = store.commitTransfer(
            careerId = CAREER,
            playerId = PLAYER,
            destinationLegacyClubId = 202,
            rosterKind = SENIOR,
            plan = plan,
            salaryAfterPurchase = 200,
        )
        assertEquals(202, after.player.clubCode)
        assertEquals(200, after.player.salaryCode)
        assertEquals(7, after.player.rawDCode)
        assertEquals("destination", requireNotNull(database.careerPlayerRuntimeDao().findMembership(CAREER, PLAYER)).clubId)
        assertEquals(6_000L, requireNotNull(store.clubFinanceState(CAREER, "source")).cash)
        assertEquals(8_000L, requireNotNull(store.clubFinanceState(CAREER, "destination")).cash)
        assertEquals(1_000L, requireNotNull(store.clubFinanceState(CAREER, "source")).ledger.playerSaleIncome)
        assertEquals(1_000L, requireNotNull(store.clubFinanceState(CAREER, "destination")).ledger.playerPurchaseExpense)

        database.close()
        database = fileDatabase(context, name)
        store = CareerManagerRuntimeStore(database)
        val runtime = requireNotNull(database.careerPlayerRuntimeDao().findRuntime(CAREER, PLAYER))
        val commercial = requireNotNull(store.playerCommercialState(CAREER, PLAYER))
        val transfer = requireNotNull(database.careerManagerRuntimeDao().findPlayerTransferState(CAREER, PLAYER))
        assertEquals(200, commercial.contract.salaryCode)
        assertEquals(plan.contractEndMillisAfter, runtime.contractEndEpochMillis)
        assertEquals(202, transfer.legacyClubCode)
        assertEquals(7, transfer.rawDCode)
        assertEquals("destination", requireNotNull(database.careerPlayerRuntimeDao().findMembership(CAREER, PLAYER)).clubId)
        assertEquals(6_000L, requireNotNull(store.clubFinanceState(CAREER, "source")).cash)
        assertEquals(8_000L, requireNotNull(store.clubFinanceState(CAREER, "destination")).cash)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `missing destination manager state rolls transfer back before membership or salary changes`() = runBlocking {
        val database = inMemoryDatabase()
        seed(database)
        val store = CareerManagerRuntimeStore(database)
        store.materializePlayerState(CAREER, PLAYER, commercial(100), transferPlayer(101, 100))
        store.materializeClubState(
            CAREER,
            "source",
            transferClub(101, 5_000L, listOf(11)),
            LegacyFinanceRuntimeState(5_000L, LegacyFinanceLedgerState()),
        )

        val error = runCatching {
            store.commitTransfer(
                careerId = CAREER,
                playerId = PLAYER,
                destinationLegacyClubId = 202,
                rosterKind = SENIOR,
                plan = purchasePlan(202, 1_000),
                salaryAfterPurchase = 200,
            )
        }.exceptionOrNull()
        assertNotNull(error)
        assertEquals("source", requireNotNull(database.careerPlayerRuntimeDao().findMembership(CAREER, PLAYER)).clubId)
        assertEquals(100, requireNotNull(store.playerCommercialState(CAREER, PLAYER)).contract.salaryCode)
        assertEquals(5_000L, requireNotNull(store.clubFinanceState(CAREER, "source")).cash)
        database.close()
        Unit
    }

    @Test
    fun `loan mutation is explicit and null never implies deletion`() = runBlocking {
        val database = inMemoryDatabase()
        seed(database)
        val store = CareerManagerRuntimeStore(database)
        val loan = CareerActiveLoanMaterialization("source", "destination", 123_456L)
        store.mutateActiveLoan(CAREER, PLAYER, CareerActiveLoanMutation.upsert(loan))
        assertEquals(loan, store.activeLoanState(CAREER, PLAYER))

        store.mutateActiveLoan(CAREER, PLAYER, CareerActiveLoanMutation.UNCHANGED)
        assertEquals(loan, store.activeLoanState(CAREER, PLAYER))

        store.mutateActiveLoan(CAREER, PLAYER, CareerActiveLoanMutation.DELETE)
        assertNull(store.activeLoanState(CAREER, PLAYER))
        database.close()
        Unit
    }

    @Test
    fun `stadium debit and ordered construction record persist and strict sweep keeps equality`() = runBlocking {
        val database = inMemoryDatabase()
        seed(database)
        val store = CareerManagerRuntimeStore(database)
        val before = LegacyFinanceRuntimeState(500_000L, LegacyFinanceLedgerState())
        store.materializeClubState(
            CAREER,
            "source",
            transferClub(101, before.cash, listOf(11)),
            before,
        )
        val quote = LegacyStadiumExpansionRule.quote(
            currentCapacities = listOf(0, 0, 0, 0),
            additions = listOf(100, 0, 0, 0),
            legacyJValue = 0,
        )
        val result = LegacyStadiumFinanceRuntimeRule.startConstruction(
            state = before,
            quote = quote,
            stadiumCode = 77,
            endTimestampMillis = 20_000L,
        )
        store.commitStadiumConstruction(CAREER, "source", before, result)
        assertEquals(result.state, store.clubFinanceState(CAREER, "source"))
        assertEquals(listOf(requireNotNull(result.recordToAppend)), store.stadiumConstructionRecords(CAREER))

        val equalitySweep = store.sweepStadiumConstructions(CAREER, 20_000L)
        assertTrue(equalitySweep.completed.isEmpty())
        assertEquals(1, store.stadiumConstructionRecords(CAREER).size)

        val completed = store.sweepStadiumConstructions(CAREER, 20_001L)
        assertEquals(1, completed.completed.size)
        assertTrue(store.stadiumConstructionRecords(CAREER).isEmpty())
        database.close()
        Unit
    }

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club("source", 101), club("destination", 202)))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Marco B",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
        database.careerPlayerRuntimeDao().upsertRuntime(runtime())
        database.careerPlayerRuntimeDao().upsertMembership(
            CareerSquadMembershipEntity(CAREER, PLAYER, "source", SENIOR, 0)
        )
    }

    private suspend fun materializeTransferFixture(store: CareerManagerRuntimeStore) {
        store.materializePlayerState(CAREER, PLAYER, commercial(100), transferPlayer(101, 100))
        store.materializeClubState(
            CAREER,
            "source",
            transferClub(101, 5_000L, listOf(11)),
            LegacyFinanceRuntimeState(5_000L, LegacyFinanceLedgerState()),
        )
        store.materializeClubState(
            CAREER,
            "destination",
            transferClub(202, 9_000L, emptyList()),
            LegacyFinanceRuntimeState(9_000L, LegacyFinanceLedgerState()),
        )
    }

    private fun purchasePlan(destinationLegacyClubId: Int, transferValue: Int) =
        LegacyTransferExecutionRule.plan(
            LegacyTransferExecutionInput(
                sourceClubPresent = true,
                sourceClubActive = true,
                destinationClubActive = true,
                destinationClubId = destinationLegacyClubId,
                transferValue = transferValue,
                legacySecondaryChargeFlag = false,
                loanMove = false,
                legacyNonFinancialMoveFlag = false,
                playerContractEndMillisBefore = 1_000L,
                currentGameMillis = 0L,
                currentCalendarMillis = 10_000L,
                sourcePrimarySlotMatchesPlayer = true,
                sourceSecondarySlotMatchesPlayer = false,
            )
        )

    private fun commercial(salary: Int) = LegacyPlayerCommercialState.fromRaw(
        salario = salary,
        rcClause = 2,
        rcRenewYear = 3,
        rcConvYear = 4,
        pendSaleClub = -1,
        pendSaleValue = 0,
        pendIsLoan = false,
    )

    private fun transferPlayer(clubCode: Int, salary: Int) = LegacyTransferPlayerRuntimeState(
        playerCode = 11,
        clubCode = clubCode,
        salaryCode = salary,
        contractEndMillis = 1_000L,
        rawX = true,
        rawY = false,
        rawZ = true,
        rawCrossActiveFlag = false,
        rawOCode = 7,
        rawDCode = 2,
    )

    private fun transferClub(
        clubCode: Int,
        cash: Long,
        roster: List<Int>,
    ) = LegacyTransferClubRuntimeState(
        clubCode = clubCode,
        active = true,
        funds = cash,
        rosterPlayerCodes = roster,
        primarySlotPlayerCode = roster.firstOrNull(),
        secondarySlotPlayerCode = null,
        rawStateFlag = true,
    )

    private fun runtime() = CareerPlayerRuntimeEntity(
        careerId = CAREER,
        playerId = PLAYER,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 25,
        overall = 80,
        marketValue = 1_000,
        star = false,
        worldTop = false,
        legacyHash = 11,
        legacyGeneratedO = 7,
        legacyCreatedYear = 0,
        contractEndEpochMillis = 1_000L,
        legacyPreviousMarketValue = 0,
        legacyQ = false,
        legacyX = true,
        legacyY = false,
        legacyZ = true,
        energy = 100,
        injuryUntilEpochDay = 0L,
    )

    private fun club(id: String, legacyId: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = id,
        name = id,
        country = 0,
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
        legacyId = legacyId,
        legacyValid = true,
    )

    private fun inMemoryDatabase(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private fun fileDatabase(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private companion object {
        const val CAREER = "career-manager"
        const val PLAYER = "player-11"
        const val SENIOR = "SENIOR"
    }
}
