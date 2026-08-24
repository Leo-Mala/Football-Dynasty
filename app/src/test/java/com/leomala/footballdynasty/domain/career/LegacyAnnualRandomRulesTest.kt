package com.leomala.footballdynasty.domain.career

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
    fun `annual gates are reproducible for the same seed`() {
        val left = StatefulJavaRandomSource(202632L)
        val right = StatefulJavaRandomSource(202632L)

        val leftSequence = List(64) { index ->
            if (index % 2 == 0) LegacyAnnualRandomRules.bestA0AGate(left)
            else LegacyAnnualRandomRules.bestA0IGate(left)
        }
        val rightSequence = List(64) { index ->
            if (index % 2 == 0) LegacyAnnualRandomRules.bestA0AGate(right)
            else LegacyAnnualRandomRules.bestA0IGate(right)
        }

        assertEquals(leftSequence, rightSequence)
        assertEquals(64L, left.draws)
        assertEquals(64L, right.draws)
    }

    @Test
    fun `annual random sequence resumes exactly from persisted snapshot`() {
        val original = StatefulJavaRandomSource(202632L)
        repeat(17) { LegacyAnnualRandomRules.bestA0AGate(original) }
        val snapshot = original.snapshot()
        val expectedTail = List(40) { index ->
            if (index % 2 == 0) LegacyAnnualRandomRules.bestA0AGate(original)
            else LegacyAnnualRandomRules.bestA0IGate(original)
        }

        val restored = StatefulJavaRandomSource.restore(snapshot)
        val actualTail = List(40) { index ->
            if (index % 2 == 0) LegacyAnnualRandomRules.bestA0AGate(restored)
            else LegacyAnnualRandomRules.bestA0IGate(restored)
        }

        assertEquals(expectedTail, actualTail)
        assertEquals(original.snapshot(), restored.snapshot())
    }
}
