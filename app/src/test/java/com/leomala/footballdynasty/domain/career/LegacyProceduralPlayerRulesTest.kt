package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyProceduralPlayerRulesTest {
    @Test
    fun `initial legacy n preserves the three smali bucket tables`() {
        assertEquals(1, LegacyProceduralPlayerRules.initialLegacyN(20, 4, 2))
        assertEquals(2, LegacyProceduralPlayerRules.initialLegacyN(20, 4, 5))
        assertEquals(4, LegacyProceduralPlayerRules.initialLegacyN(20, 4, 6))
        assertEquals(10, LegacyProceduralPlayerRules.initialLegacyN(20, 4, 100))
        assertEquals(1, LegacyProceduralPlayerRules.initialLegacyN(15, 3, 2))
        assertEquals(2, LegacyProceduralPlayerRules.initialLegacyN(15, 3, 5))
        assertEquals(4, LegacyProceduralPlayerRules.initialLegacyN(15, 3, 6))
        assertEquals(10, LegacyProceduralPlayerRules.initialLegacyN(15, 3, 100))
        assertEquals(1, LegacyProceduralPlayerRules.initialLegacyN(14, 3, 4))
        assertEquals(2, LegacyProceduralPlayerRules.initialLegacyN(14, 3, 8))
        assertEquals(3, LegacyProceduralPlayerRules.initialLegacyN(14, 3, 9))
        assertEquals(9, LegacyProceduralPlayerRules.initialLegacyN(14, 3, 100))
    }

    @Test
    fun `requested legacy e overrides result but not the random draw`() {
        val random = QueueRandomSource(99)
        val selected = LegacyProceduralPlayerRules.rollLegacyE(random, requestedLegacyE = 2)
        assertEquals(100, selected.rawRoll1To100)
        assertEquals(2, selected.legacyE)
        assertEquals(listOf(100), random.bounds)
    }

    @Test
    fun `legacy pair table uses exact per-position bounds`() {
        val random = QueueRandomSource(11)
        val pair = LegacyProceduralPlayerRules.rollLegacyPair(random, legacyE = 3)
        assertEquals(LegacyProceduralPlayerRules.LegacyPair(10, 11), pair)
        assertEquals(listOf(12), random.bounds)
    }

    @Test
    fun `special legacy d branch preserves selector fallback and changed-d n draw`() {
        val random = QueueRandomSource(5, 137, 2)
        val generatedD = LegacyProceduralPlayerRules.rewriteLegacyD(random, 29, 18, 1)
        val generatedN = LegacyProceduralPlayerRules.maybeRewriteLegacyNForChangedD(random, 4, 29, generatedD)
        assertEquals(137, generatedD)
        assertEquals(9, generatedN)
        assertEquals(listOf(6, 200, 4), random.bounds)
    }

    @Test
    fun `special legacy d branch consumes no rng when structural guard fails`() {
        val random = QueueRandomSource()
        assertEquals(29, LegacyProceduralPlayerRules.rewriteLegacyD(random, 29, 17, 1))
        assertEquals(0L, random.draws)
    }

    @Test
    fun `legacy h preserves greater-than-one-hundred reset to ninety-five`() {
        val random = QueueRandomSource(4)
        assertEquals(95, LegacyProceduralPlayerRules.legacyH(random, legacyC = 20, legacyN = 25))
        assertEquals(listOf(5), random.bounds)
    }

    @Test
    fun `legacy g preserves thresholds and clamps to one through ten`() {
        assertEquals(10, LegacyProceduralPlayerRules.legacyG(QueueRandomSource(99), legacyN = 10))
        assertEquals(1, LegacyProceduralPlayerRules.legacyG(QueueRandomSource(15), legacyN = 1))
        assertEquals(5, LegacyProceduralPlayerRules.legacyG(QueueRandomSource(14), legacyN = 5))
    }

    @Test
    fun `legacy D consumes intermediate h g draws and optional b draw`() {
        val plain = QueueRandomSource(0, 0, 0)
        val plainResult = LegacyProceduralPlayerRules.executeLegacyD(plain, false, 0, 4, 20, false, 18, 5)
        assertEquals(17, plainResult.legacyF)
        assertEquals(listOf(3, 5, 100), plain.bounds)
        val flagged = QueueRandomSource(0, 0, 0, 0)
        LegacyProceduralPlayerRules.executeLegacyD(flagged, false, 0, 4, 20, true, 18, 5)
        assertEquals(listOf(3, 3, 5, 100), flagged.bounds)
    }

    @Test
    fun `annual structural draft preserves draw order including name boundary`() {
        val random = QueueRandomSource(0, 2, 0, 0, 0, 3, 4, 1, 2, 1, 0, 14, 4, 60)
        val draft = LegacyProceduralPlayerRules.generateAnnualDraft(
            random,
            LegacyProceduralPlayerRules.TargetContext(20, 4, false, 0, 29, 3),
        ) { legacyD, rng -> "N$legacyD-${rng.nextInt(7)}" }
        assertEquals(10, draft.legacyN)
        assertTrue(draft.legacyB)
        assertEquals(18, draft.legacyC)
        assertEquals(3, draft.legacyE)
        assertEquals(4, draft.legacyJ)
        assertEquals(11, draft.legacyL)
        assertEquals(11, draft.legacyD)
        assertEquals("N11-4", draft.name)
        assertEquals(1, draft.legacyG)
        assertEquals(29, draft.legacyF)
        assertEquals(70, draft.legacyO)
        assertEquals(10, draft.legacyM)
        assertEquals(listOf(100, 4, 100, 12, 6, 4, 7, 2, 3, 3, 5, 100, 5, 100), random.bounds)
        assertEquals(14L, random.draws)
        assertFalse(random.bounds.isEmpty())
    }

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val values = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set
        override fun nextInt(bound: Int): Int {
            check(values.isNotEmpty()) { "No queued value for bound=$bound" }
            val value = values.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }
        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
