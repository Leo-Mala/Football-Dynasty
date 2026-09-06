package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualBestFSourceRulesTest {
    @Test
    fun `mode zero preserves national group first then p zero source order`() {
        val random = FixedIntRandomSource(99)
        val national = group("national", 9)
        val groups = listOf(group("g0-a", 0), group("g3", 3), group("g0-b", 0))

        val pools =
            LegacyAnnualBestFSourceRules.buildGroupPools(
                random = random,
                mode = 0,
                currentLegacyJ = 2,
                currentLegacyJ0 = 29,
                currentLegacyR0 = true,
                subjectOverall = 60,
                subjectO0 = false,
                subjectW0 = false,
                legacyNationalGroup = national,
                allGroups = groups,
            )

        assertEquals(listOf("national", "g0-a", "g0-b"), pools.primary.map { it.id })
        assertTrue(pools.secondary.isEmpty())
        assertEquals(listOf("g0-a", "g0-b"), pools.tertiary.map { it.id })
        assertEquals(1L, random.draws)
    }

    @Test
    fun `mode zero non expanding primary leaves p zero groups in secondary and tertiary`() {
        val random = FixedIntRandomSource(0)
        val groups = listOf(group("g0-a", 0), group("g3", 3), group("g0-b", 0))

        val pools =
            LegacyAnnualBestFSourceRules.buildGroupPools(
                random = random,
                mode = 0,
                currentLegacyJ = 2,
                currentLegacyJ0 = 29,
                currentLegacyR0 = false,
                subjectOverall = 60,
                subjectO0 = false,
                subjectW0 = false,
                legacyNationalGroup = null,
                allGroups = groups,
            )

        assertTrue(pools.primary.isEmpty())
        assertEquals(listOf("g0-a", "g0-b"), pools.secondary.map { it.id })
        assertEquals(listOf("g0-a", "g0-b"), pools.tertiary.map { it.id })
    }

    @Test
    fun `mode one raw J filters append without duplicating existing primary groups`() {
        val random = FixedIntRandomSource()
        val national = group("g0-a", 0)
        val groups = listOf(group("g0-a", 0), group("g3", 3), group("g5", 5), group("g4", 4))

        val pools =
            LegacyAnnualBestFSourceRules.buildGroupPools(
                random = random,
                mode = 1,
                currentLegacyJ = 3,
                currentLegacyJ0 = 10,
                currentLegacyR0 = true,
                subjectOverall = 20,
                subjectO0 = true,
                subjectW0 = false,
                legacyNationalGroup = national,
                allGroups = groups,
            )

        assertEquals(listOf("g0-a", "g3"), pools.primary.map { it.id })
        assertTrue(pools.secondary.isEmpty())
        assertEquals(listOf("g0-a"), pools.tertiary.map { it.id })
        assertEquals(0L, random.draws)
    }

    @Test
    fun `mode two is exactly all p zero groups and consumes no constructor RNG`() {
        val random = FixedIntRandomSource()
        val groups = listOf(group("a", 0), group("b", 3), group("c", 0))

        val pools =
            LegacyAnnualBestFSourceRules.buildGroupPools(
                random = random,
                mode = 2,
                currentLegacyJ = 99,
                currentLegacyJ0 = 29,
                currentLegacyR0 = true,
                subjectOverall = 99,
                subjectO0 = false,
                subjectW0 = false,
                legacyNationalGroup = null,
                allGroups = groups,
            )

        assertEquals(listOf("a", "c"), pools.primary.map { it.id })
        assertTrue(pools.secondary.isEmpty())
        assertTrue(pools.tertiary.isEmpty())
        assertEquals(0L, random.draws)
    }

    @Test
    fun `q candidate collection preserves group and club source order plus D0 true signal`() {
        val g1 =
            group(
                id = "g1",
                legacyP = 0,
                legacyA0 = 3,
                clubs =
                    listOf(
                        club("current", legacyO = 2),
                        club("a", legacyO = 1),
                        club("full", legacyO = 2, rosterSize = 30),
                    ),
            )
        val g2 =
            group(
                id = "g2",
                legacyP = 0,
                legacyA0 = 3,
                clubs = listOf(club("b", legacyO = 3), club("q0", legacyO = 2, legacyQ0 = true)),
            )

        val candidates =
            LegacyAnnualBestFSourceRules.collectQCandidates(
                groups = listOf(g1, g2),
                currentClubId = "current",
                currentLegacyO = 2,
                currentLegacyJ = 0,
                currentLegacyP0 = 2,
                subjectOverall = 50,
                mode = 0,
            )

        assertEquals(listOf("a", "b"), candidates.map { it.club.id })
        assertTrue(candidates.all { it.markLegacyD0True })
    }

    @Test
    fun `q low overall uses each groups raw A0 and keeps duplicate club occurrence`() {
        val duplicate = club("same", legacyO = 3)
        val candidates =
            LegacyAnnualBestFSourceRules.collectQCandidates(
                groups =
                    listOf(
                        group("g1", 0, legacyA0 = 2, clubs = listOf(duplicate)),
                        group("g2", 0, legacyA0 = 3, clubs = listOf(duplicate)),
                    ),
                currentClubId = "current",
                currentLegacyO = 5,
                currentLegacyJ = 0,
                currentLegacyP0 = 0,
                subjectOverall = 5,
                mode = 0,
            )

        assertEquals(listOf("same"), candidates.map { it.club.id })
    }

    @Test
    fun `fallback pool preserves only non R0 non Q0 clubs below thirty in source order`() {
        val candidates =
            LegacyAnnualBestFSourceRules.collectFallbackCandidates(
                listOf(
                    club("a", legacyO = 1),
                    club("r0", legacyO = 1, legacyR0 = true),
                    club("q0", legacyO = 1, legacyQ0 = true),
                    club("full", legacyO = 1, rosterSize = 30),
                    club("b", legacyO = 1),
                ),
            )

        assertEquals(listOf("a", "b"), candidates.map { it.id })
    }

    private fun group(
        id: String,
        legacyP: Int,
        legacyA0: Int = 5,
        clubs: List<LegacyAnnualBestFSourceRules.Club<String>> = emptyList(),
    ) = LegacyAnnualBestFSourceRules.Group(id = id, legacyP = legacyP, legacyA0 = legacyA0, clubs = clubs)

    private fun club(
        id: String,
        legacyO: Int,
        legacyP0: Int = 0,
        legacyR0: Boolean = false,
        legacyQ0: Boolean = false,
        rosterSize: Int = 10,
    ) =
        LegacyAnnualBestFSourceRules.Club(
            id = id,
            legacyO = legacyO,
            legacyP0 = legacyP0,
            legacyR0 = legacyR0,
            legacyQ0 = legacyQ0,
            rosterSize = rosterSize,
        )

    private class FixedIntRandomSource(
        vararg values: Int,
    ) : RandomSource {
        private val values = values.toList()
        private var index = 0

        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = values[index++]
            require(value in 0 until bound) { "value=$value bound=$bound" }
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
