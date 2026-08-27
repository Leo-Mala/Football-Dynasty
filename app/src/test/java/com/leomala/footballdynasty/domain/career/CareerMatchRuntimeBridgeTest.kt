package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.model.Match
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerMatchRuntimeBridgeTest {
    @Test
    fun `same career rng and schedule replay the same resolved match`() {
        val initial = CareerStateFactory.create("phase9-determinism", 987654321L)
        val firstDay = initial.calendar.currentDayIndex
        val secondDay = firstDay + 3
        val schedule = listOf(
            ScheduledCareerMatch("m1", firstDay, 1, "club-a", "club-b"),
            ScheduledCareerMatch("m2", secondDay, 1, "club-c", "club-d"),
        )

        fun run(): CareerMatchRuntimeResult = CareerMatchRuntimeBridge.run(
            state = initial,
            schedule = schedule,
            matchId = "m1",
        ) { scheduled, random ->
            Match(
                id = scheduled.matchId,
                homeClubId = scheduled.homeClubId,
                awayClubId = scheduled.awayClubId,
                homeGoals = random.nextInt(5),
                awayGoals = random.nextInt(5),
            )
        }

        val first = run()
        val replay = run()

        assertEquals(first, replay)
        assertEquals(secondDay, first.state.calendar.currentDayIndex)
        assertEquals(secondDay, first.nextPlayableDayIndex)
        assertEquals(initial.random.draws + 2L, first.state.random.draws)
        assertTrue(first.schedule.single { it.matchId == "m1" }.processed)
        assertFalse(first.schedule.single { it.matchId == "m2" }.processed)
        assertNotEquals(initial.random.internalState, first.state.random.internalState)
    }

    @Test
    fun `another unprocessed match on the same day keeps the career on that day`() {
        val initial = CareerStateFactory.create("phase9-same-day", 123L)
        val day = initial.calendar.currentDayIndex
        val schedule = listOf(
            ScheduledCareerMatch("m1", day, 7, "club-a", "club-b"),
            ScheduledCareerMatch("m2", day, 7, "club-c", "club-d"),
        )

        val result = CareerMatchRuntimeBridge.run(initial, schedule, "m1") { scheduled, _ ->
            Match(scheduled.matchId, scheduled.homeClubId, scheduled.awayClubId, 1, 0)
        }

        assertEquals(day, result.state.calendar.currentDayIndex)
        assertEquals(day, result.nextPlayableDayIndex)
        assertTrue(result.schedule.single { it.matchId == "m1" }.processed)
        assertFalse(result.schedule.single { it.matchId == "m2" }.processed)
    }

    @Test
    fun `cannot skip an earlier playable match day`() {
        val initial = CareerStateFactory.create("phase9-order", 321L)
        val firstDay = initial.calendar.currentDayIndex
        val laterDay = firstDay + 5
        val schedule = listOf(
            ScheduledCareerMatch("earlier", firstDay, 1, "club-a", "club-b"),
            ScheduledCareerMatch("later", laterDay, 1, "club-c", "club-d"),
        )

        assertThrows(IllegalArgumentException::class.java) {
            CareerMatchRuntimeBridge.run(initial, schedule, "later") { scheduled, _ ->
                Match(scheduled.matchId, scheduled.homeClubId, scheduled.awayClubId, 0, 0)
            }
        }
    }

    @Test
    fun `runtime result must preserve scheduled match and club identity`() {
        val initial = CareerStateFactory.create("phase9-identity", 456L)
        val day = initial.calendar.currentDayIndex
        val schedule = listOf(
            ScheduledCareerMatch("m1", day, 1, "club-a", "club-b"),
        )

        assertThrows(IllegalArgumentException::class.java) {
            CareerMatchRuntimeBridge.run(initial, schedule, "m1") { scheduled, _ ->
                Match(scheduled.matchId, "wrong-home", scheduled.awayClubId, 2, 1)
            }
        }
    }
}
