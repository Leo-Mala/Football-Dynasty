package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualRandomRules.BestA0JRandomSite
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualRandomRulesTest {
    @Test
    fun `direct annual gates consume one bounded draw`() {
        val random = StatefulJavaRandomSource(202632L)
        LegacyAnnualRandomRules.bestA0AGate(random)
        LegacyAnnualRandomRules.bestA0IGate(random)
        LegacyAnnualRandomRules.bestFConstructorGate(random)
        LegacyAnnualRandomRules.bestFNGate(random)
        assertEquals(4L, random.draws)
    }

    @Test
    fun `best a0 j sites retain the eight smali thresholds`() {
        assertEquals(
            listOf(10, 90, 30, 30, 35, 45, 75, 95),
            BestA0JRandomSite.entries.map { it.thresholdExclusive },
        )
    }

    @Test
    fun `each best a0 j site consumes exactly one bounded draw`() {
        BestA0JRandomSite.entries.forEach { site ->
            val random = StatefulJavaRandomSource(202632L)
            LegacyAnnualRandomRules.bestA0JGate(random, site)
            assertEquals("site=$site", 1L, random.draws)
        }
    }

    @Test
    fun `deterministic annual shuffle consumes size minus one draws`() {
        val random = StatefulJavaRandomSource(202632L)
        val values = (0 until 12).toMutableList()
        LegacyAnnualRandomRules.shuffleInPlace(values, random)
        assertEquals(11L, random.draws)
        assertEquals((0 until 12).toSet(), values.toSet())
    }

    @Test
    fun `annual random operations are reproducible for the same seed`() {
        fun sequence(random: StatefulJavaRandomSource): Pair<List<Boolean>, List<Int>> {
            val gates = buildList {
                repeat(4) {
                    add(LegacyAnnualRandomRules.bestA0AGate(random))
                    add(LegacyAnnualRandomRules.bestA0IGate(random))
                    add(LegacyAnnualRandomRules.bestFConstructorGate(random))
                    add(LegacyAnnualRandomRules.bestFNGate(random))
                    BestA0JRandomSite.entries.forEach { site ->
                        add(LegacyAnnualRandomRules.bestA0JGate(random, site))
                    }
                }
            }
            val shuffled = (0 until 16).toMutableList()
            LegacyAnnualRandomRules.shuffleInPlace(shuffled, random)
            return gates to shuffled
        }

        val left = StatefulJavaRandomSource(202632L)
        val right = StatefulJavaRandomSource(202632L)
        assertEquals(sequence(left), sequence(right))
        assertEquals(left.snapshot(), right.snapshot())
    }

    @Test
    fun `annual random operations resume exactly from persisted snapshot`() {
        val original = StatefulJavaRandomSource(202632L)
        repeat(17) { LegacyAnnualRandomRules.bestA0AGate(original) }
        val snapshot = original.snapshot()

        fun tail(random: StatefulJavaRandomSource): Pair<List<Boolean>, List<Int>> {
            val gates = BestA0JRandomSite.entries.map { site ->
                LegacyAnnualRandomRules.bestA0JGate(random, site)
            } + listOf(
                LegacyAnnualRandomRules.bestFConstructorGate(random),
                LegacyAnnualRandomRules.bestFNGate(random),
            )
            val shuffled = (0 until 10).toMutableList()
            LegacyAnnualRandomRules.shuffleInPlace(shuffled, random)
            return gates to shuffled
        }

        val expected = tail(original)
        val restored = StatefulJavaRandomSource.restore(snapshot)
        val actual = tail(restored)
        assertEquals(expected, actual)
        assertEquals(original.snapshot(), restored.snapshot())
    }
}
