package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyCoachSeasonClubRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerCoachEmploymentPersistenceTest {
    @Test
    fun `outgoing then incoming employment persists V9 V11 slices and survives reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase14-employment-v11-reopen"
        context.deleteDatabase(name)
        var database = database(context, name)
        seed(database)
        var tickets = CareerTicketRuntimeStore(database)
        var coaches = CareerCoachRuntimeStore(database)

        tickets.materializeManagers(
            CAREER,
            listOf(
                CareerManagerTicketRuntimeState(0, OUTGOING_ID, 61),
                CareerManagerTicketRuntimeState(1, INCOMING_ID, 25),
            ),
        )
        tickets.materializeClubState(CAREER, TARGET, CareerClubTicketRuntimeState(3, OUTGOING_ID))
        val outgoingBefore = coachState(0, OUTGOING_ID, 61, user = true, currentClubId = TARGET)
        val incomingBefore = coachState(1, INCOMING_ID, 25, user = false, currentClubId = null, rawG = 44, rawM = 9)
        coaches.materialize(CAREER, outgoingBefore)
        coaches.materialize(CAREER, incomingBefore)

        val outgoingAfter = outgoingBefore.copy(
            currentClubId = null,
            previousClubId = TARGET,
            previousClubCountry = 7,
            previousClubDivisionIndex = 2,
        )
        val incomingAfter = incomingBefore.copy(
            currentClubId = TARGET,
            rawG = 100,
            rawH = 80,
            rawM = 0,
        )
        coaches.commitEmploymentTransition(
            careerId = CAREER,
            targetClubId = TARGET,
            expectedClubState = CareerClubTicketRuntimeState(3, OUTGOING_ID),
            clubLegacyManagerIdAfter = INCOMING_ID,
            updatesInLegacyOrder = listOf(
                CareerCoachEmploymentUpdate(CareerCoachEmploymentRole.OUTGOING, outgoingBefore, outgoingAfter),
                CareerCoachEmploymentUpdate(CareerCoachEmploymentRole.INCOMING, incomingBefore, incomingAfter),
            ),
        )

        assertEquals(outgoingAfter, coaches.find(CAREER, 0))
        assertEquals(incomingAfter, coaches.find(CAREER, 1))
        assertEquals(CareerClubTicketRuntimeState(3, INCOMING_ID), tickets.findClubState(CAREER, TARGET))
        assertEquals(listOf(61, 80), tickets.managersInWorldOrder(CAREER).map { it.rawH })

        database.close()
        database = database(context, name)
        tickets = CareerTicketRuntimeStore(database)
        coaches = CareerCoachRuntimeStore(database)
        assertEquals(outgoingAfter, coaches.find(CAREER, 0))
        assertEquals(incomingAfter, coaches.find(CAREER, 1))
        assertEquals(CareerClubTicketRuntimeState(3, INCOMING_ID), tickets.findClubState(CAREER, TARGET))
        assertEquals(listOf(61, 80), tickets.managersInWorldOrder(CAREER).map { it.rawH })

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `stale incoming rolls back outgoing H coach and club association`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seed(database)
        val tickets = CareerTicketRuntimeStore(database)
        val coaches = CareerCoachRuntimeStore(database)
        tickets.materializeManagers(
            CAREER,
            listOf(
                CareerManagerTicketRuntimeState(0, OUTGOING_ID, 61),
                CareerManagerTicketRuntimeState(1, INCOMING_ID, 25),
            ),
        )
        tickets.materializeClubState(CAREER, TARGET, CareerClubTicketRuntimeState(3, OUTGOING_ID))
        val outgoingBefore = coachState(0, OUTGOING_ID, 61, user = true, currentClubId = TARGET)
        val incomingBefore = coachState(1, INCOMING_ID, 25, user = false, currentClubId = null, rawG = 44, rawM = 9)
        coaches.materialize(CAREER, outgoingBefore)
        coaches.materialize(CAREER, incomingBefore)

        val error = runCatching {
            coaches.commitEmploymentTransition(
                careerId = CAREER,
                targetClubId = TARGET,
                expectedClubState = CareerClubTicketRuntimeState(3, OUTGOING_ID),
                clubLegacyManagerIdAfter = INCOMING_ID,
                updatesInLegacyOrder = listOf(
                    CareerCoachEmploymentUpdate(
                        CareerCoachEmploymentRole.OUTGOING,
                        outgoingBefore,
                        outgoingBefore.copy(
                            currentClubId = null,
                            previousClubId = TARGET,
                            previousClubCountry = 7,
                            previousClubDivisionIndex = 2,
                        ),
                    ),
                    CareerCoachEmploymentUpdate(
                        CareerCoachEmploymentRole.INCOMING,
                        incomingBefore.copy(rawG = 45),
                        incomingBefore.copy(currentClubId = TARGET, rawG = 100, rawH = 80, rawM = 0),
                    ),
                ),
            )
        }.exceptionOrNull()

        assertNotNull(error)
        assertEquals(outgoingBefore, coaches.find(CAREER, 0))
        assertEquals(incomingBefore, coaches.find(CAREER, 1))
        assertEquals(CareerClubTicketRuntimeState(3, OUTGOING_ID), tickets.findClubState(CAREER, TARGET))
        assertEquals(listOf(61, 25), tickets.managersInWorldOrder(CAREER).map { it.rawH })
        database.close()
        Unit
    }

    @Test
    fun `employment persistence rejects unrelated mutation wrong order and wrong club manager result`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seed(database)
        val tickets = CareerTicketRuntimeStore(database)
        val coaches = CareerCoachRuntimeStore(database)
        tickets.materializeManagers(
            CAREER,
            listOf(
                CareerManagerTicketRuntimeState(0, OUTGOING_ID, 61),
                CareerManagerTicketRuntimeState(1, INCOMING_ID, 25),
            ),
        )
        tickets.materializeClubState(CAREER, TARGET, CareerClubTicketRuntimeState(3, OUTGOING_ID))
        val outgoing = coachState(0, OUTGOING_ID, 61, currentClubId = TARGET)
        val incoming = coachState(1, INCOMING_ID, 25, user = false, currentClubId = null, rawG = 44, rawM = 9)
        coaches.materialize(CAREER, outgoing)
        coaches.materialize(CAREER, incoming)

        assertNotNull(
            runCatching {
                coaches.commitEmploymentTransition(
                    CAREER,
                    TARGET,
                    CareerClubTicketRuntimeState(3, OUTGOING_ID),
                    INCOMING_ID,
                    listOf(
                        CareerCoachEmploymentUpdate(
                            CareerCoachEmploymentRole.INCOMING,
                            incoming,
                            incoming.copy(currentClubId = TARGET, rawG = 100, rawH = 80, rawD = 99, rawM = 0),
                        )
                    ),
                )
            }.exceptionOrNull()
        )
        assertNotNull(
            runCatching {
                coaches.commitEmploymentTransition(
                    CAREER,
                    TARGET,
                    CareerClubTicketRuntimeState(3, OUTGOING_ID),
                    INCOMING_ID,
                    listOf(
                        CareerCoachEmploymentUpdate(
                            CareerCoachEmploymentRole.INCOMING,
                            incoming,
                            incoming.copy(currentClubId = TARGET, rawG = 100, rawH = 80, rawM = 0),
                        ),
                        CareerCoachEmploymentUpdate(
                            CareerCoachEmploymentRole.OUTGOING,
                            outgoing,
                            outgoing.copy(currentClubId = null, previousClubId = TARGET),
                        ),
                    ),
                )
            }.exceptionOrNull()
        )
        assertNotNull(
            runCatching {
                coaches.commitEmploymentTransition(
                    CAREER,
                    TARGET,
                    CareerClubTicketRuntimeState(3, OUTGOING_ID),
                    999,
                    listOf(
                        CareerCoachEmploymentUpdate(
                            CareerCoachEmploymentRole.INCOMING,
                            incoming,
                            incoming.copy(currentClubId = TARGET, rawG = 100, rawH = 80, rawM = 0),
                        )
                    ),
                )
            }.exceptionOrNull()
        )

        assertEquals(outgoing, coaches.find(CAREER, 0))
        assertEquals(incoming, coaches.find(CAREER, 1))
        assertEquals(CareerClubTicketRuntimeState(3, OUTGOING_ID), tickets.findClubState(CAREER, TARGET))
        database.close()
        Unit
    }

    private fun database(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club(TARGET, 101)))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Coach employment V11",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
    }

    private fun coachState(
        ordinal: Int,
        managerId: Int,
        rawH: Int,
        user: Boolean = true,
        currentClubId: String? = TARGET,
        rawG: Int = 60,
        rawM: Int = 4,
    ) = CareerCoachRuntimeState(
        sourceOrdinal = ordinal,
        legacyManagerId = managerId,
        isUserControlled = user,
        currentClubId = currentClubId,
        alternativeClubId = "selection-a",
        previousClubId = "club-old",
        previousClubCountry = 29,
        previousClubDivisionIndex = 1,
        rawG = rawG,
        rawH = rawH,
        rawD = 10,
        rawE = 4,
        rawF = 3,
        rawO = 20,
        rawM = rawM,
        records = listOf(
            LegacyCoachSeasonClubRecord(2026, 10, rawMatches = 4, rawWins = 2, rawLosses = 1, rawPoints = 9),
        ),
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
        capacity = 10_000,
        reputation = 3,
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

    private companion object {
        const val CAREER = "career-coach-employment-v11"
        const val TARGET = "club-a"
        const val OUTGOING_ID = 7
        const val INCOMING_ID = 8
    }
}
