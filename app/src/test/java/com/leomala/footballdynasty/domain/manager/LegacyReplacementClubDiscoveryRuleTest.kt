package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyReplacementClubDiscoveryRuleTest {
    private class FakeRandom(values: List<Int>) : RandomSource {
        private val ints = ArrayDeque(values)
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            bounds += bound
            draws++
            val value = ints.removeFirst()
            require(value in 0 until bound) { "$value outside 0 until $bound" }
            return value
        }
        override fun nextBoolean(): Boolean = error("unused")
        override fun nextDouble(): Double = error("unused")
    }

    private fun club(id: String, blocked: Boolean = false) =
        LegacyReplacementSearchClub(identityKey = id, value = id, rawQ0 = blocked)

    private fun competition(prefix: String, size: Int = 8) =
        LegacyReplacementSearchCompetition((0 until size).map { club("$prefix$it") })

    private fun manager(
        human: Boolean = false,
        rawD: Int = 0,
        rawW: Int = 1,
        currentDivision: Int? = 1,
        rawU: Int = 10,
        rawE: Int = 20,
        excluded: String? = null,
    ) = LegacyReplacementSearchManager(
        rawU = rawU,
        rawE = rawE,
        userControlled = human,
        rawD = rawD,
        rawW = rawW,
        currentClubDivisionValue = currentDivision,
        excludedClubIdentityKey = excluded,
    )

    @Test
    fun competitionH0PreservesPrimaryWindowFilterShuffleBoundsAndFirstUnseenRule() {
        val random = FakeRandom(listOf(0, 0, 0))
        val output = mutableListOf<LegacyReplacementSearchClub<String>>()
        LegacyReplacementClubDiscoveryRule.collectFromCompetition(
            competition("c"),
            manager(excluded = "c4"),
            primaryWindow = true,
            output = output,
            random = random,
        )

        // Primary window is indexes 2..<6; c4 is excluded. Fisher-Yates over [c2,c3,c5]
        // with zeros yields c3 first.
        assertEquals(listOf("c3"), output.map { it.value })
        assertEquals(listOf(3, 2), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun secondaryCompetitionWindowDoesNotRepairLegacyNegativeStart() {
        LegacyReplacementClubDiscoveryRule.collectFromCompetition(
            LegacyReplacementSearchCompetition(listOf(club("a"), club("b"), club("c"))),
            manager(),
            primaryWindow = false,
            output = mutableListOf(),
            random = FakeRandom(emptyList()),
        )
    }

    @Test
    fun contextH0UsesExact1000ThresholdTablesAndCompetitionShuffleOrder() {
        val context = LegacyReplacementSearchContext(
            rawY = 10,
            rawU = 20,
            competitions = listOf(
                competition("a"),
                competition("b"),
                competition("c"),
            ),
        )
        // draw 701 => primaryAbove700 row 0 = [0,-1,-1,2,-1,-1].
        // Each selected competition contributes one candidate and shuffles four eligible window clubs.
        val random = FakeRandom(listOf(701, 0, 0, 0, 0, 0, 0))
        val output = mutableListOf<LegacyReplacementSearchClub<String>>()
        LegacyReplacementClubDiscoveryRule.collectFromContext(
            context,
            manager(),
            primaryWindow = true,
            primaryContext = true,
            output = output,
            random = random,
        )

        assertEquals(listOf("a3", "c3"), output.map { it.value })
        assertEquals(listOf(1000, 4, 3, 2, 4, 3, 2), random.bounds)
    }

    @Test
    fun secondaryContextOverwritesPrimaryThresholdChoiceAndResolvesEightToLastCompetition() {
        val context = LegacyReplacementSearchContext(
            rawY = 10,
            rawU = 20,
            competitions = listOf(
                competition("a"),
                competition("b"),
                competition("last"),
            ),
        )
        // Division index 2 + secondary context + draw >500 => [8,8,-1...], where 8 becomes last index.
        val random = FakeRandom(listOf(900, 0, 0, 0, 0, 0, 0))
        val output = mutableListOf<LegacyReplacementSearchClub<String>>()
        LegacyReplacementClubDiscoveryRule.collectFromContext(
            context,
            manager(currentDivision = 3),
            primaryWindow = true,
            primaryContext = false,
            output = output,
            random = random,
        )

        // Same competition is visited twice. Both shuffles yield the same first candidate; the
        // second call sees the duplicate and then advances to the next unseen candidate.
        assertEquals(listOf("last3", "last4"), output.map { it.value })
        assertEquals(listOf(1000, 4, 3, 2, 4, 3, 2), random.bounds)
    }

    @Test
    fun emptyCompetitionUsesFallbackOnlyForNonHumanAndFallbackChecksOnlyShuffledFirst() {
        val context = LegacyReplacementSearchContext(
            rawY = 10,
            rawU = 20,
            competitions = listOf(LegacyReplacementSearchCompetition(emptyList())),
            fallbackClubs = listOf(club("fallback")),
        )

        val nonHumanOutput = mutableListOf<LegacyReplacementSearchClub<String>>()
        val nonHumanRandom = FakeRandom(listOf(0))
        LegacyReplacementClubDiscoveryRule.collectFromContext(
            context,
            manager(human = false),
            primaryWindow = true,
            primaryContext = true,
            output = nonHumanOutput,
            random = nonHumanRandom,
        )
        assertEquals(listOf("fallback"), nonHumanOutput.map { it.value })
        assertEquals(listOf(1000), nonHumanRandom.bounds)

        val humanOutput = mutableListOf<LegacyReplacementSearchClub<String>>()
        val humanRandom = FakeRandom(listOf(0))
        LegacyReplacementClubDiscoveryRule.collectFromContext(
            context,
            manager(human = true),
            primaryWindow = true,
            primaryContext = true,
            output = humanOutput,
            random = humanRandom,
        )
        assertTrue(humanOutput.isEmpty())
    }

    @Test
    fun worldBVisitsU0BeforeDistinctT0WithTrueThenFalseContextModes() {
        val t0 = LegacyReplacementSearchContext(
            rawY = 10,
            rawU = 99,
            competitions = listOf(competition("t")),
        )
        val u0 = LegacyReplacementSearchContext(
            rawY = 99,
            rawU = 20,
            competitions = listOf(competition("u")),
        )
        // U0: draw 701 primary-context row -> competition 0 once.
        // T0: draw 0 secondary-context row -> competition 0 once (index 1 is invalid).
        val random = FakeRandom(listOf(701, 0, 0, 0, 0, 0, 0, 0))
        val result = LegacyReplacementClubDiscoveryRule.collectFromWorld(
            listOf(t0, u0),
            manager(),
            primaryWindow = true,
            random = random,
        )

        assertEquals(listOf("u3", "t3"), result)
        assertEquals(listOf(1000, 4, 3, 2, 1000, 4, 3, 2), random.bounds)
    }

    @Test
    fun expandedSearchPredicatePreservesK_D_andClampedWBranches() {
        assertFalse(LegacyReplacementClubDiscoveryRule.shouldSearchOtherContexts(manager(human = false, rawD = 0, rawW = 9)))
        assertTrue(LegacyReplacementClubDiscoveryRule.shouldSearchOtherContexts(manager(human = true, rawD = 1, rawW = 1)))
        assertFalse(LegacyReplacementClubDiscoveryRule.shouldSearchOtherContexts(manager(human = true, rawD = 2, rawW = 3)))
        assertTrue(LegacyReplacementClubDiscoveryRule.shouldSearchOtherContexts(manager(human = true, rawD = 2, rawW = 4)))
        // Legacy w() clamps >5 to 5 before comparison; it still remains >3.
        assertTrue(LegacyReplacementClubDiscoveryRule.shouldSearchOtherContexts(manager(human = true, rawD = 5, rawW = 99)))
    }

    @Test
    fun contextWithNoCompetitionsConsumesNoRandomDraw() {
        val random = FakeRandom(emptyList())
        val output = mutableListOf<LegacyReplacementSearchClub<String>>()
        LegacyReplacementClubDiscoveryRule.collectFromContext(
            LegacyReplacementSearchContext(rawY = 1, rawU = 2, competitions = emptyList()),
            manager(),
            primaryWindow = true,
            primaryContext = true,
            output = output,
            random = random,
        )
        assertTrue(output.isEmpty())
        assertEquals(0L, random.draws)
    }
}
