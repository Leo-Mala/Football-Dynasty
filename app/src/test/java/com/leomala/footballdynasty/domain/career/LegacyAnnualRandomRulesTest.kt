package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualRandomRules.BestA0JRandomSite
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualRandomRulesTest {
    @Test
    fun `best a0 a gate consumes exactly one bounded draw`() {
        val random = StatefulJavaRandomSource(202632L)
        LegacyAnnualRandomRules.bestA0AGate(random)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `best a0 i gate consumes exactly one bounded draw`() {
        val random = StatefulJavaRandomSource(202632L)
        LegacyAnnualRandomRules.bestA0IGate(random)
        assertEquals(1L, random.draws)
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
    fun `annual gates are reproducible for the same seed`() {
        val left = StatefulJavaRandomSource(202632L)
        val right = StatefulJavaRandomSource(202632L)

        fun sequence(random: StatefulJavaRandomSource): List<Boolean> = buildList {
            repeat(8) {
                add(LegacyAnnualRandomRules.bestA0AGate(random))
                add(LegacyAnnualRandomRules.bestA0IGate(random))
                BestA0JRandomSite.entries.forEach { site ->
                    add(LegacyAnnualRandomRules.bestA0JGate(random, site))
                }
            }
        }

        assertEquals(sequence(left), sequence(right))
        assertEquals(left.draws, right.draws)
        assertEquals(80L, left.draws)
    }

    @Test
    fun `annual random sequence resumes exactly from persisted snapshot`() {
        val original = StatefulJavaRandomSource(202632L)
        repeat(17) { LegacyAnnualRandomRules.bestA0AGate(original) }
        val snapshot = original.snapshot()

        fun tail(random: StatefulJavaRandomSource): List<Boolean> = buildList {
            repeat(4) {
                add(LegacyAnnualRandomRules.bestA0IGate(random))
                BestA0JRandomSite.entries.forEach { site ->
                    add(LegacyAnnualRandomRules.bestA0JGate(random, site))
                }
            }
        }

        val expectedTail = tail(original)
        val restored = StatefulJavaRandomSource.restore(snapshot)
        val actualTail = tail(restored)

        assertEquals(expectedTail, actualTail)
        assertEquals(original.snapshot(), restored.snapshot())
    }
}
