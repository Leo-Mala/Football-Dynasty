package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LegacyMatchSubstitutionRulesTest {
    @Test
    fun `direct substitution keeps original player as outgoing without RNG`() {
        val original = player("out", g0 = 2, l0 = 1, f0 = 1, r = 7)
        val incoming = player("in", g0 = 9, l0 = 1, f0 = 1, r = 7)
        val random = QueueRandomSource()

        val plan = LegacyMatchSubstitutionRules.resolve(
            original = original,
            active = listOf(original),
            bench = listOf(incoming),
            automaticOutgoing = false,
            enforceLegacyL0Compatibility = false,
            random = random,
        )

        requireNotNull(plan)
        assertSame(original, plan.outgoing)
        assertSame(incoming, plan.incoming)
        assertEquals(2, plan.finalIncomingPositionIndex)
        assertEquals(-1, plan.ignoredLegacyTrailingArgument)
        assertEquals(LegacyMatchEventType.SUBSTITUTION, plan.eventType)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `automatic outgoing first searches positions eighteen through twenty five`() {
        val original = player("original", g0 = 2, l0 = 1, f0 = 1, r = 7)
        val eighteen = player("18", g0 = 18, l0 = 0, f0 = 0, r = 0)
        val nineteen = player("19", g0 = 19, l0 = 0, f0 = 0, r = 0)
        val incoming = player("in", g0 = 9, l0 = 1, f0 = 1, r = 7)
        val random = QueueRandomSource(0)

        val plan = LegacyMatchSubstitutionRules.resolve(
            original = original,
            active = listOf(eighteen, nineteen),
            bench = listOf(incoming),
            automaticOutgoing = true,
            enforceLegacyL0Compatibility = false,
            random = random,
        )

        requireNotNull(plan)
        assertEquals("19", plan.outgoing.value)
        assertEquals(listOf(2), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `automatic outgoing falls back to fourteen through seventeen`() {
        val original = player("original", g0 = 2, l0 = 1, f0 = 1, r = 7)
        val fifteen = player("15", g0 = 15, l0 = 0, f0 = 0, r = 0)
        val incoming = player("in", g0 = 9, l0 = 1, f0 = 1, r = 7)
        val random = QueueRandomSource()

        val plan = LegacyMatchSubstitutionRules.resolve(
            original = original,
            active = listOf(fifteen),
            bench = listOf(incoming),
            automaticOutgoing = true,
            enforceLegacyL0Compatibility = false,
            random = random,
        )

        requireNotNull(plan)
        assertEquals("15", plan.outgoing.value)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `goalkeeper legacy g0 one unlocks final two through twenty five fallback`() {
        val original = player("original", g0 = 1, l0 = 0, f0 = 4, r = 5)
        val two = player("2", g0 = 2, l0 = 0, f0 = 0, r = 0)
        val incoming = player("in", g0 = 9, l0 = 0, f0 = 4, r = 5)
        val random = QueueRandomSource()

        val plan = LegacyMatchSubstitutionRules.resolve(
            original = original,
            active = listOf(two),
            bench = listOf(incoming),
            automaticOutgoing = true,
            enforceLegacyL0Compatibility = false,
            random = random,
        )

        requireNotNull(plan)
        assertEquals("2", plan.outgoing.value)
    }

    @Test
    fun `non goalkeeper does not use final two through twenty five fallback`() {
        val original = player("original", g0 = 2, l0 = 1, f0 = 1, r = 7)
        val two = player("2", g0 = 2, l0 = 0, f0 = 0, r = 0)
        val incoming = player("in", g0 = 9, l0 = 1, f0 = 1, r = 7)

        val plan = LegacyMatchSubstitutionRules.resolve(
            original = original,
            active = listOf(two),
            bench = listOf(incoming),
            automaticOutgoing = true,
            enforceLegacyL0Compatibility = false,
            random = QueueRandomSource(),
        )

        assertNull(plan)
    }

    @Test
    fun `bench scan uses original player position rather than automatically selected outgoing`() {
        val original = player("original", g0 = 2, l0 = 1, f0 = 1, r = 7)
        val outgoing = player("out", g0 = 18, l0 = 0, f0 = 9, r = 9)
        val matchOriginal = player("match-original", g0 = 5, l0 = 1, f0 = 1, r = 7)
        val matchOutgoing = player("match-outgoing", g0 = 5, l0 = 4, f0 = 1, r = 9)

        val plan = LegacyMatchSubstitutionRules.resolve(
            original = original,
            active = listOf(outgoing),
            bench = listOf(matchOutgoing, matchOriginal),
            automaticOutgoing = true,
            enforceLegacyL0Compatibility = false,
            random = QueueRandomSource(),
        )

        requireNotNull(plan)
        assertEquals("match-original", plan.incoming.value)
        assertEquals(2, plan.finalIncomingPositionIndex)
    }

    @Test
    fun `bench selector preserves first matching candidate order`() {
        val first = player("first", g0 = 9, l0 = 1, f0 = 1, r = 4)
        val second = player("second", g0 = 9, l0 = 1, f0 = 1, r = 8)

        val selected = LegacyMatchSubstitutionRules.selectBenchCandidate(
            candidates = listOf(first, second),
            originalPositionIndex = 2,
            requireLegacyL0 = true,
        )

        assertSame(first, selected)
    }

    @Test
    fun `bench selector progressively relaxes f0 on its second mode`() {
        val candidate = player("relaxed", g0 = 9, l0 = 1, f0 = 8, r = 3)

        val selected = LegacyMatchSubstitutionRules.selectBenchCandidate(
            candidates = listOf(candidate),
            originalPositionIndex = 2,
            requireLegacyL0 = true,
        )

        assertSame(candidate, selected)
    }

    @Test
    fun `compatibility gate rejects zero l0 incoming when outgoing l0 is nonzero`() {
        val original = player("original", g0 = 0, l0 = 0, f0 = 2, r = 3)
        val outgoing = player("out", g0 = 18, l0 = 1, f0 = 9, r = 9)
        val incoming = player("in", g0 = 9, l0 = 0, f0 = 2, r = 3)

        val plan = LegacyMatchSubstitutionRules.resolve(
            original = original,
            active = listOf(outgoing),
            bench = listOf(incoming),
            automaticOutgoing = true,
            enforceLegacyL0Compatibility = true,
            random = QueueRandomSource(),
        )

        assertNull(plan)
    }

    @Test
    fun `original position zero leaves outgoing position as final o1 assignment`() {
        val original = player("original", g0 = 0, l0 = 0, f0 = 2, r = 3)
        val outgoing = player("out", g0 = 18, l0 = 0, f0 = 9, r = 9)
        val incoming = player("in", g0 = 9, l0 = 0, f0 = 2, r = 3)

        val plan = LegacyMatchSubstitutionRules.resolve(
            original = original,
            active = listOf(outgoing),
            bench = listOf(incoming),
            automaticOutgoing = true,
            enforceLegacyL0Compatibility = false,
            random = QueueRandomSource(),
        )

        requireNotNull(plan)
        assertEquals(18, plan.finalIncomingPositionIndex)
    }

    @Test
    fun `positive original position overrides outgoing position in o1`() {
        val original = player("original", g0 = 2, l0 = 1, f0 = 1, r = 7)
        val outgoing = player("out", g0 = 18, l0 = 0, f0 = 9, r = 9)
        val incoming = player("in", g0 = 9, l0 = 1, f0 = 1, r = 7)

        val plan = LegacyMatchSubstitutionRules.resolve(
            original = original,
            active = listOf(outgoing),
            bench = listOf(incoming),
            automaticOutgoing = true,
            enforceLegacyL0Compatibility = true,
            random = QueueRandomSource(),
        )

        requireNotNull(plan)
        assertEquals(2, plan.finalIncomingPositionIndex)
        assertEquals(5, plan.ignoredLegacyTrailingArgument)
    }

    @Test
    fun `o1 mutation order keeps decrement before list mutations and event last`() {
        assertEquals(
            listOf(
                LegacyMatchSubstitutionRules.MutationOperation.DECREMENT_SUBSTITUTION_COUNT,
                LegacyMatchSubstitutionRules.MutationOperation.SET_INCOMING_TO_OUTGOING_POSITION,
                LegacyMatchSubstitutionRules.MutationOperation.OVERRIDE_INCOMING_POSITION_WHEN_POSITIVE,
                LegacyMatchSubstitutionRules.MutationOperation.REMOVE_OUTGOING_FROM_ACTIVE,
                LegacyMatchSubstitutionRules.MutationOperation.ADD_INCOMING_TO_ACTIVE,
                LegacyMatchSubstitutionRules.MutationOperation.ADD_INCOMING_TO_USED,
                LegacyMatchSubstitutionRules.MutationOperation.MARK_INCOMING_SELECTED,
                LegacyMatchSubstitutionRules.MutationOperation.REMOVE_INCOMING_FROM_BENCH,
                LegacyMatchSubstitutionRules.MutationOperation.EMIT_SUBSTITUTION_EVENT,
            ),
            LegacyMatchSubstitutionRules.mutationOrder,
        )
    }

    private fun player(value: String, g0: Int, l0: Int, f0: Int, r: Int) =
        LegacyMatchSubstitutionRules.Player(value, g0, l0, f0, r)

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
