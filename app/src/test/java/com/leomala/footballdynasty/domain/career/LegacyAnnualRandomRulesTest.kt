package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualRandomRules.BestA0JRandomSite
import com.leomala.footballdynasty.domain.career.LegacyAnnualRandomRules.BestFNRoute
import com.leomala.footballdynasty.foundation.random.RandomSource
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
    fun `best f constructor mode zero evaluates qualifying random gate before flags`() {
        val random = FixedIntRandomSource(0)
        val expands = LegacyAnnualRandomRules.bestFConstructorExpandsPrimaryGroup(
            random = random,
            mode = 0,
            legacyJ = 4,
            legacyJ0 = 29,
            subjectO = 51,
            subjectO0 = true,
            subjectW0 = false,
        )

        assertEquals(true, expands)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `best f constructor mode one flags short circuit random gate`() {
        val random = FixedIntRandomSource(0)
        val expands = LegacyAnnualRandomRules.bestFConstructorExpandsPrimaryGroup(
            random = random,
            mode = 1,
            legacyJ = 4,
            legacyJ0 = 29,
            subjectO = 51,
            subjectO0 = true,
            subjectW0 = false,
        )

        assertEquals(true, expands)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `best f constructor gate is skipped when legacy qualifiers fail`() {
        val random = FixedIntRandomSource(99)
        val expands = LegacyAnnualRandomRules.bestFConstructorExpandsPrimaryGroup(
            random = random,
            mode = 0,
            legacyJ = 4,
            legacyJ0 = 28,
            subjectO = 80,
            subjectO0 = false,
            subjectW0 = false,
        )

        assertEquals(false, expands)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `best f constructor mode two always uses primary group without rng`() {
        val random = FixedIntRandomSource(99)
        val expands = LegacyAnnualRandomRules.bestFConstructorExpandsPrimaryGroup(
            random = random,
            mode = 2,
            legacyJ = 5,
            legacyJ0 = 29,
            subjectO = 99,
            subjectO0 = false,
            subjectW0 = false,
        )

        assertEquals(true, expands)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `best f n skips rng when O is at most thirty`() {
        val random = FixedIntRandomSource(99)
        val route = LegacyAnnualRandomRules.bestFNRoute(
            random = random,
            subjectO = 30,
            subjectO0 = false,
            currentQ0 = true,
        )

        assertEquals(BestFNRoute.G_THEN_OPTIONAL_H, route)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `best f n skips rng when current Q0 is false`() {
        val random = FixedIntRandomSource(99)
        val route = LegacyAnnualRandomRules.bestFNRoute(
            random = random,
            subjectO = 90,
            subjectO0 = false,
            currentQ0 = false,
        )

        assertEquals(BestFNRoute.G_THEN_OPTIONAL_H, route)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `best f n O0 Q0 override occurs after a qualifying rng draw`() {
        val random = FixedIntRandomSource(0)
        val route = LegacyAnnualRandomRules.bestFNRoute(
            random = random,
            subjectO = 31,
            subjectO0 = true,
            currentQ0 = true,
        )

        assertEquals(BestFNRoute.OPTIONAL_I_THEN_OPTIONAL_H_THEN_G, route)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `best f n threshold sixty selects primary and sixty one selects alternate`() {
        val primaryRandom = FixedIntRandomSource(60)
        val alternateRandom = FixedIntRandomSource(61)

        assertEquals(
            BestFNRoute.G_THEN_OPTIONAL_H,
            LegacyAnnualRandomRules.bestFNRoute(primaryRandom, 31, false, true),
        )
        assertEquals(
            BestFNRoute.OPTIONAL_I_THEN_OPTIONAL_H_THEN_G,
            LegacyAnnualRandomRules.bestFNRoute(alternateRandom, 31, false, true),
        )
        assertEquals(1L, primaryRandom.draws)
        assertEquals(1L, alternateRandom.draws)
    }

    @Test
    fun `best f q range preserves mode and low O overrides`() {
        assertEquals(
            LegacyAnnualRandomRules.BestFQRange(9, 11),
            LegacyAnnualRandomRules.bestFQRange(0, 10, 0, 4, 50, 7),
        )
        assertEquals(
            LegacyAnnualRandomRules.BestFQRange(1, 1),
            LegacyAnnualRandomRules.bestFQRange(1, 10, 0, 4, 40, 7),
        )
        assertEquals(
            LegacyAnnualRandomRules.BestFQRange(1, 2),
            LegacyAnnualRandomRules.bestFQRange(1, 10, 1, 4, 40, 7),
        )
        assertEquals(
            LegacyAnnualRandomRules.BestFQRange(0, 7),
            LegacyAnnualRandomRules.bestFQRange(1, 10, 1, 2, 20, 7),
        )
    }

    @Test
    fun `best f q candidate filter enforces range identity flags and roster cap`() {
        val range = LegacyAnnualRandomRules.BestFQRange(2, 4)
        assertEquals(
            true,
            LegacyAnnualRandomRules.bestFQCandidateEligible(3, range, false, false, 29),
        )
        assertEquals(
            false,
            LegacyAnnualRandomRules.bestFQCandidateEligible(5, range, false, false, 29),
        )
        assertEquals(
            false,
            LegacyAnnualRandomRules.bestFQCandidateEligible(3, range, true, false, 29),
        )
        assertEquals(
            false,
            LegacyAnnualRandomRules.bestFQCandidateEligible(3, range, false, true, 29),
        )
        assertEquals(
            false,
            LegacyAnnualRandomRules.bestFQCandidateEligible(3, range, false, false, 30),
        )
    }

    @Test
    fun `best f p fallback eligibility preserves flags and roster cap`() {
        assertEquals(true, LegacyAnnualRandomRules.bestFPFallbackEligible(false, false, 29))
        assertEquals(false, LegacyAnnualRandomRules.bestFPFallbackEligible(true, false, 29))
        assertEquals(false, LegacyAnnualRandomRules.bestFPFallbackEligible(false, true, 29))
        assertEquals(false, LegacyAnnualRandomRules.bestFPFallbackEligible(false, false, 30))
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

    private class FixedIntRandomSource(
        vararg values: Int,
    ) : RandomSource {
        private val iterator = values.iterator()

        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = iterator.nextInt()
            require(value in 0 until bound) { "value=$value bound=$bound" }
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used by this characterization")

        override fun nextDouble(): Double = error("not used by this characterization")
    }
}
