package com.leomala.footballdynasty.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerLeagueRoundTransientRatingRuntimeTest {
    @Test
    fun `transient z2 buffer accumulates in insertion order and clears only after round close`() {
        val runtime = CareerLeagueRoundTransientRatingRuntime()
        val key = CareerLeagueRoundTransientRatingRuntime.Key("career", "league", 1)

        val first = runtime.stage(
            key,
            listOf(
                capture("p1", "a", 8.0, 1, 10),
                capture("p2", "b", 7.5, 9, 20),
            ),
        )
        assertEquals(listOf(0, 1), first.candidatesInLegacyOrder.map { it.stableOrdinal })
        assertTrue(runtime.buffered(key).isEmpty())

        runtime.commit(first, roundClosed = false)
        assertEquals(listOf("p1", "p2"), runtime.buffered(key).map { it.playerId })

        val second = runtime.stage(
            key,
            listOf(capture("p3", "c", 7.0, 3, 30)),
        )
        assertEquals(listOf("p1", "p2", "p3"), second.candidatesInLegacyOrder.map { it.playerId })
        assertEquals(listOf(0, 1, 2), second.candidatesInLegacyOrder.map { it.stableOrdinal })

        runtime.commit(second, roundClosed = true)
        assertTrue(runtime.buffered(key).isEmpty())
    }

    @Test
    fun `failed durable commit leaves previously committed transient buffer unchanged`() {
        val runtime = CareerLeagueRoundTransientRatingRuntime()
        val key = CareerLeagueRoundTransientRatingRuntime.Key("career", "league", 1)
        val committed = runtime.stage(key, listOf(capture("p1", "a", 8.0, 1, 10)))
        runtime.commit(committed, roundClosed = false)

        runtime.stage(key, listOf(capture("p2", "b", 7.0, 9, 20)))
        // Deliberately do not call commit(): this models a Room transaction that threw/rolled back.
        assertEquals(listOf("p1"), runtime.buffered(key).map { it.playerId })
    }

    @Test
    fun `committing a newer round discards stale buffer from the same competition`() {
        val runtime = CareerLeagueRoundTransientRatingRuntime()
        val firstKey = CareerLeagueRoundTransientRatingRuntime.Key("career", "league", 1)
        val secondKey = CareerLeagueRoundTransientRatingRuntime.Key("career", "league", 2)
        runtime.commit(
            runtime.stage(firstKey, listOf(capture("p1", "a", 8.0, 1, 10))),
            roundClosed = false,
        )

        runtime.commit(
            runtime.stage(secondKey, listOf(capture("p2", "b", 7.0, 9, 20))),
            roundClosed = false,
        )

        assertTrue(runtime.buffered(firstKey).isEmpty())
        assertEquals(listOf("p2"), runtime.buffered(secondKey).map { it.playerId })
    }

    private fun capture(
        playerId: String,
        clubId: String,
        rating: Double,
        g0: Int,
        randomOrder: Int,
    ) = CareerLeagueRoundTransientRatingRuntime.CaptureInput(
        playerId = playerId,
        clubIdAtSnapshot = clubId,
        ratingY0 = rating,
        legacyG0 = g0,
        randomOrder = randomOrder,
    )
}
