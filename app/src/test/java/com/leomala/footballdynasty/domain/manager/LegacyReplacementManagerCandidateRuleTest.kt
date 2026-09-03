package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyReplacementManagerCandidateRuleTest {
    private data class FakeRandom(
        val ints: ArrayDeque<Int>,
        override var draws: Long = 0,
    ) : RandomSource {
        override fun nextInt(bound: Int): Int {
            draws += 1
            val value = ints.removeFirst()
            require(value in 0 until bound) { "$value outside 0 until $bound" }
            return value
        }
        override fun nextBoolean(): Boolean = error("unused")
        override fun nextDouble(): Double = error("unused")
    }

    private fun candidate(
        id: String,
        club: String? = null,
        human: Boolean = false,
        primary: Int = 7,
        secondary: Int = 99,
        level: Int = 3,
        v: Int = 0,
        h: Int = 0,
        s: Int = 0,
    ) = LegacyReplacementManagerCandidate(id, club, human, primary, secondary, level, v, h, s)

    private val target = LegacyReplacementTargetClub(countryCode = 7, levelCode = 3)

    @Test
    fun modeBoundsPreserveSmaliModeTwoFallthroughAndUnknownZeroLocals() {
        assertEquals(0..5, LegacyReplacementManagerCandidateRule.modeBounds(-1, 3))
        assertEquals(3..3, LegacyReplacementManagerCandidateRule.modeBounds(0, 3))
        assertEquals(2..3, LegacyReplacementManagerCandidateRule.modeBounds(1, 3))
        assertEquals(1..4, LegacyReplacementManagerCandidateRule.modeBounds(2, 3))
        assertEquals(0..0, LegacyReplacementManagerCandidateRule.modeBounds(91, 3))
    }

    @Test
    fun modeTwoIncludesTargetPlusOneButStillExcludesTargetPlusTwo() {
        val random = FakeRandom(ArrayDeque(listOf(0)))
        val selected = LegacyReplacementManagerCandidateRule.select(
            listOf(
                candidate("plus-two", level = 5, v = 100),
                candidate("plus-one", level = 4, v = 50),
            ),
            target,
            2,
            random,
        )
        assertEquals("plus-one", selected)
        assertEquals(1L, random.draws)
    }

    @Test
    fun emptyPoolConsumesNoRandomDraw() {
        val random = FakeRandom(ArrayDeque())
        assertNull(
            LegacyReplacementManagerCandidateRule.select(
                listOf(candidate("employed", club = "club"), candidate("human", human = true)),
                target,
                0,
                random,
            ),
        )
        assertEquals(0L, random.draws)
    }

    @Test
    fun draw49UsesStableLegacyComparatorVDescHDescSAsc() {
        val random = FakeRandom(ArrayDeque(listOf(49)))
        val selected = LegacyReplacementManagerCandidateRule.select(
            listOf(
                candidate("a", v = 4, h = 7, s = 9),
                candidate("b", v = 5, h = 1, s = 0),
                candidate("c", v = 5, h = 3, s = 8),
                candidate("d", v = 5, h = 3, s = 2),
            ),
            target,
            0,
            random,
        )
        assertEquals("d", selected)
        assertEquals(1L, random.draws)
    }

    @Test
    fun draw50UsesCollectionsStyleFisherYatesAndExactBounds() {
        // decision 50 -> shuffle; then nextInt(3)=0, nextInt(2)=1.
        val random = FakeRandom(ArrayDeque(listOf(50, 0, 1)))
        val selected = LegacyReplacementManagerCandidateRule.select(
            listOf(candidate("a"), candidate("b"), candidate("c")),
            target,
            0,
            random,
        )
        assertEquals("c", selected)
        assertEquals(3L, random.draws)
    }

    @Test
    fun filterRequiresUnemployedNonHumanCountryMatchAndInclusiveLevel() {
        val random = FakeRandom(ArrayDeque(listOf(0)))
        val selected = LegacyReplacementManagerCandidateRule.select(
            listOf(
                candidate("wrong-country", primary = 8, secondary = 9),
                candidate("too-low", level = 0),
                candidate("human", human = true, level = 2),
                candidate("employed", club = "x", level = 2),
                candidate("secondary-country", primary = 8, secondary = 7, level = 1),
            ),
            target,
            2,
            random,
        )
        assertEquals("secondary-country", selected)
    }

    @Test
    fun fallbackReturnsFirstUnemployedNonHumanWithoutCountryLevelOrRng() {
        val selected = LegacyReplacementManagerCandidateRule.firstUnemployedNonHuman(
            listOf(
                candidate("human", human = true),
                candidate("employed", club = "x"),
                candidate("first", primary = -50, secondary = -60, level = 99),
                candidate("second"),
            ),
        )
        assertEquals("first", selected)
    }
}
