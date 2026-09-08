package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeBridge
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.competition.LegacyCompetitionSnapshotRules
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.model.Match
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerCompetitionRoundSnapshotPersistenceTest {
    @Test
    fun `last match persists only h0 projection and snapshot survives reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase16-round-h0-reopen"
        context.deleteDatabase(name)
        var database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        database.clubDao().upsertAll(listOf(club("a"), club("b"), club("c"), club("d")))
        RoomCareerRepository(database) { 10L }.save(Career("career-round", "Round", null, null))

        val initial = CareerStateFactory.create("career-round", 123456L)
        val day = initial.calendar.currentDayIndex
        val schedule = listOf(
            ScheduledCareerMatch("m1", day, 1, "a", "b"),
            ScheduledCareerMatch("m2", day, 1, "c", "d"),
        )
        val matchStore = CareerMatchStore(database)
        matchStore.initializeSchedule(initial, schedule)
        CareerCompetitionStore(database).initializeLeague(
            careerId = "career-round",
            competitionId = "league-1",
            legacyCompetitionType = 1,
            legacyFormatCode = -1,
            clubIds = listOf("a", "b", "c", "d"),
            roundMatchIds = listOf(listOf("m1", "m2")),
        )

        val firstCandidates = listOf(
            candidate("p1", "a", 8.0, 1, 10, 0),
            candidate("p2", "b", 7.0, 9, 20, 1),
        )
        val first = CareerMatchRuntimeBridge.run(initial, schedule, "m1") { scheduled, _ ->
            Match(scheduled.matchId, scheduled.homeClubId, scheduled.awayClubId, 2, 0)
        }
        val firstOutcome = matchStore.commitMatch(
            result = first,
            roundSnapshotStaging = CareerRoundSnapshotStaging(
                competitionId = "league-1",
                roundNumber = 1,
                legacySeasonIndex = initial.season.number,
                candidatesInLegacyOrder = firstCandidates,
            ),
        )
        assertNull(firstOutcome.advancedCompetitionId)
        assertEquals(
            0,
            database.careerLegacyDurabilityDao()
                .snapshots("career-round", "league-1", CareerLegacyDurabilityStore.SNAPSHOT_ROUND)
                .size,
        )

        val fullCandidates = firstCandidates + listOf(
            candidate("p3", "c", 7.5, 3, 30, 2),
            candidate("p4", "d", 6.0, 25, 40, 3),
        )
        val second = CareerMatchRuntimeBridge.run(first.state, first.schedule, "m2") { scheduled, _ ->
            Match(scheduled.matchId, scheduled.homeClubId, scheduled.awayClubId, 1, 1)
        }
        val secondOutcome = matchStore.commitMatch(
            result = second,
            roundSnapshotStaging = CareerRoundSnapshotStaging(
                competitionId = "league-1",
                roundNumber = 1,
                legacySeasonIndex = initial.season.number,
                candidatesInLegacyOrder = fullCandidates,
            ),
        )
        assertEquals("league-1", secondOutcome.advancedCompetitionId)
        assertEquals(1, secondOutcome.advancedRoundNumber)

        val snapshot = database.careerLegacyDurabilityDao()
            .snapshots("career-round", "league-1", CareerLegacyDurabilityStore.SNAPSHOT_ROUND)
            .single()
        assertEquals(initial.season.number, snapshot.legacyA)
        assertEquals(-1, snapshot.legacyB)
        val members = database.careerLegacyDurabilityDao().snapshotMembers(
            "career-round",
            "league-1",
            CareerLegacyDurabilityStore.SNAPSHOT_ROUND,
            snapshot.snapshotOrdinal,
        )
        assertEquals(listOf("p1", "p2", "p3", "p4"), members.map { it.playerId })
        assertEquals(listOf("a", "b", "c", "d"), members.map { it.clubIdAtSnapshot })

        database.close()
        database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        val reopened = database.careerLegacyDurabilityDao()
            .snapshots("career-round", "league-1", CareerLegacyDurabilityStore.SNAPSHOT_ROUND)
            .single()
        assertEquals(snapshot, reopened)
        assertEquals(
            listOf("p1", "p2", "p3", "p4"),
            database.careerLegacyDurabilityDao().snapshotMembers(
                "career-round",
                "league-1",
                CareerLegacyDurabilityStore.SNAPSHOT_ROUND,
                reopened.snapshotOrdinal,
            ).map { it.playerId },
        )

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    private fun candidate(
        playerId: String,
        clubId: String,
        rating: Double,
        g0: Int,
        randomOrder: Int,
        stableOrdinal: Int,
    ) = LegacyCompetitionSnapshotRules.RoundCandidate(
        playerId = playerId,
        clubIdAtSnapshot = clubId,
        ratingY0 = rating,
        legacyG0 = g0,
        randomOrder = randomOrder,
        stableOrdinal = stableOrdinal,
    )

    private fun club(id: String) = ClubEntity(
        id = id, dataVersion = 1, importScope = null, sourceFileRef = id, name = id,
        country = 0, state = 0, level = 1, stadium = "", capacity = 0, reputation = 0,
        primaryColor = "", secondaryColor = "", coach = "", coachCountry = 0, baseColor = 0,
        legacyAid = 0, legacySid = 0, legacyTid = 0, legacyVid = 0, legacyId = 0, legacyValid = true,
    )
}
