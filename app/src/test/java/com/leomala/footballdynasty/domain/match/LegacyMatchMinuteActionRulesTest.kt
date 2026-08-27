package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchMinuteActionRulesTest {
    @Test
    fun `refresh occurs before candidate snapshot and C selector`() {
        val log = mutableListOf<String>()
        val random = QueueRandomSource(0)
        val result = LegacyMatchMinuteActionRules.apply(
            decision = decision(LegacyMatchMinuteRules.Action.LEGACY_C, refresh = true),
            counters = counters(),
            random = random,
            activeCandidates = {
                log += "candidates"
                listOf(candidate("p", 10))
            },
            refreshPlayerState = { log += "refresh" },
            applyLegacyC = { log += "c:$it" },
        )

        assertEquals(listOf("refresh", "candidates", "c:p"), log)
        assertEquals(
            listOf(
                LegacyMatchMinuteActionRules.Operation.REFRESH_PLAYER_STATE,
                LegacyMatchMinuteActionRules.Operation.SELECT_S,
                LegacyMatchMinuteActionRules.Operation.INCREMENT_O,
                LegacyMatchMinuteActionRules.Operation.APPLY_C,
            ),
            result.operations,
        )
    }

    @Test
    fun `C uses S selector bound and increments O even when no player is selected`() {
        val random = QueueRandomSource(99)
        var applied = 0
        val result = LegacyMatchMinuteActionRules.apply<String>(
            decision = decision(LegacyMatchMinuteRules.Action.LEGACY_C),
            counters = counters(o = 5),
            random = random,
            activeCandidates = { emptyList() },
            applyLegacyC = { applied++ },
        )

        assertEquals(listOf(100), random.bounds)
        assertEquals(6, result.counters.legacyO)
        assertEquals(0, applied)
        assertNull(result.selectedPlayer)
        assertEquals(
            listOf(
                LegacyMatchMinuteActionRules.Operation.SELECT_S,
                LegacyMatchMinuteActionRules.Operation.INCREMENT_O,
            ),
            result.operations,
        )
    }

    @Test
    fun `C selected player is applied after O mutation in recovered operation plan`() {
        val player = Any()
        val result = LegacyMatchMinuteActionRules.apply(
            decision = decision(LegacyMatchMinuteRules.Action.LEGACY_C),
            counters = counters(o = 2),
            random = QueueRandomSource(0),
            activeCandidates = { listOf(candidate(player, 10)) },
        )

        assertSame(player, result.selectedPlayer)
        assertEquals(3, result.counters.legacyO)
        assertEquals(
            listOf(
                LegacyMatchMinuteActionRules.Operation.SELECT_S,
                LegacyMatchMinuteActionRules.Operation.INCREMENT_O,
                LegacyMatchMinuteActionRules.Operation.APPLY_C,
            ),
            result.operations,
        )
    }

    @Test
    fun `D uses U selector and increments P after applying selected player`() {
        val player = Any()
        var applied = 0
        val random = QueueRandomSource(0)
        val result = LegacyMatchMinuteActionRules.apply(
            decision = decision(LegacyMatchMinuteRules.Action.LEGACY_D),
            counters = counters(p = 7),
            random = random,
            activeCandidates = { listOf(candidate(player, 1)) },
            applyLegacyD = { applied++ },
        )

        assertEquals(listOf(200), random.bounds)
        assertSame(player, result.selectedPlayer)
        assertEquals(1, applied)
        assertEquals(8, result.counters.legacyP)
        assertEquals(
            listOf(
                LegacyMatchMinuteActionRules.Operation.SELECT_U,
                LegacyMatchMinuteActionRules.Operation.APPLY_D,
                LegacyMatchMinuteActionRules.Operation.INCREMENT_P,
            ),
            result.operations,
        )
    }

    @Test
    fun `D increments P even when U returns null`() {
        val result = LegacyMatchMinuteActionRules.apply<String>(
            decision = decision(LegacyMatchMinuteRules.Action.LEGACY_D),
            counters = counters(p = 3),
            random = QueueRandomSource(0),
            activeCandidates = { emptyList() },
        )

        assertEquals(4, result.counters.legacyP)
        assertNull(result.selectedPlayer)
        assertEquals(
            listOf(
                LegacyMatchMinuteActionRules.Operation.SELECT_U,
                LegacyMatchMinuteActionRules.Operation.INCREMENT_P,
            ),
            result.operations,
        )
    }

    @Test
    fun `type five increments Q before T selection and application`() {
        val player = Any()
        val random = QueueRandomSource(0)
        val result = LegacyMatchMinuteActionRules.apply(
            decision = decision(LegacyMatchMinuteRules.Action.LEGACY_TYPE_5),
            counters = counters(q = 8),
            random = random,
            activeCandidates = { listOf(candidate(player, 1)) },
        )

        assertEquals(listOf(500), random.bounds)
        assertEquals(9, result.counters.legacyQ)
        assertSame(player, result.selectedPlayer)
        assertEquals(
            listOf(
                LegacyMatchMinuteActionRules.Operation.INCREMENT_Q,
                LegacyMatchMinuteActionRules.Operation.SELECT_T,
                LegacyMatchMinuteActionRules.Operation.APPLY_TYPE_5,
            ),
            result.operations,
        )
    }

    @Test
    fun `type five keeps Q increment when T returns null`() {
        val result = LegacyMatchMinuteActionRules.apply<String>(
            decision = decision(LegacyMatchMinuteRules.Action.LEGACY_TYPE_5),
            counters = counters(q = 0),
            random = QueueRandomSource(0),
            activeCandidates = { emptyList() },
        )

        assertEquals(1, result.counters.legacyQ)
        assertNull(result.selectedPlayer)
        assertEquals(
            listOf(
                LegacyMatchMinuteActionRules.Operation.INCREMENT_Q,
                LegacyMatchMinuteActionRules.Operation.SELECT_T,
            ),
            result.operations,
        )
    }

    @Test
    fun `second half J invokes only J callback and leaves counters unchanged`() {
        var calls = 0
        val before = counters(o = 1, p = 2, q = 3)
        val result = LegacyMatchMinuteActionRules.apply<String>(
            decision = decision(LegacyMatchMinuteRules.Action.SECOND_HALF_J),
            counters = before,
            random = QueueRandomSource(),
            activeCandidates = { error("candidate provider must not be read") },
            applySecondHalfJ = { calls++ },
        )

        assertEquals(1, calls)
        assertEquals(before, result.counters)
        assertEquals(listOf(LegacyMatchMinuteActionRules.Operation.APPLY_SECOND_HALF_J), result.operations)
    }

    @Test
    fun `none performs no selector callback counter or RNG work`() {
        val random = QueueRandomSource()
        val before = counters(o = 4, p = 5, q = 6)
        val result = LegacyMatchMinuteActionRules.apply<String>(
            decision = decision(LegacyMatchMinuteRules.Action.NONE),
            counters = before,
            random = random,
            activeCandidates = { error("candidate provider must not be read") },
        )

        assertEquals(before, result.counters)
        assertTrue(result.operations.isEmpty())
        assertEquals(0L, random.draws)
    }

    private fun <T> candidate(value: T, position: Int) =
        LegacyMatchPlayerSelectionRules.Candidate(value, position)

    private fun counters(o: Int = 0, p: Int = 0, q: Int = 0) =
        LegacyMatchMinuteActionRules.Counters(o, p, q)

    private fun decision(
        action: LegacyMatchMinuteRules.Action,
        refresh: Boolean = false,
    ) = LegacyMatchMinuteRules.Decision(
        side = LegacyMatchMinuteRules.Side.HOME,
        action = action,
        refreshPlayerState = refresh,
        primaryBound = 1,
        secondaryBound = 1,
        tertiaryBound = 1,
    )

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val queue = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(queue.isNotEmpty()) { "No queued RNG value for bound=$bound" }
            val value = queue.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
