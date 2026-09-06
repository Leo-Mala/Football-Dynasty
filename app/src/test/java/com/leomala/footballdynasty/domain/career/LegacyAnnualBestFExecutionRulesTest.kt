package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyAnnualBestFExecutionRulesTest {
    @Test
    fun `G route shuffles primary q pool marks D0 and selects first Z0 eligible club`() {
        val random = IdentityShuffleRandomSource()
        val g =
            group(
                "g",
                listOf(
                    club("full", legacyO = 2, rosterSize = 30),
                    club("target", legacyO = 2),
                ),
            )
        val pools = LegacyAnnualBestFSourceRules.GroupPools(primary = listOf(g), secondary = emptyList(), tertiary = emptyList())

        val result =
            select(
                random = random,
                subjectOverall = 20,
                subjectO0 = false,
                currentP0 = 2,
                pools = pools,
                allClubs = emptyList(),
            )

        assertEquals(LegacyAnnualSelectionRules.BestFNRoute.G_THEN_OPTIONAL_H, result.route)
        assertEquals("target", result.selected?.id)
        assertEquals(listOf("target"), result.qAttempts.single().markedLegacyD0True)
        assertNull(result.fallbackAttempt)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `alternate route tries tertiary first without n RNG when O0 and current Q0 are true`() {
        val random = IdentityShuffleRandomSource()
        val tertiary = group("i", listOf(club("target", legacyO = 2)))
        val pools = LegacyAnnualBestFSourceRules.GroupPools<String, String>(
            primary = emptyList(),
            secondary = emptyList(),
            tertiary = listOf(tertiary),
        )

        val result =
            select(
                random = random,
                subjectOverall = 50,
                subjectO0 = true,
                currentP0 = 2,
                currentQ0 = true,
                pools = pools,
                allClubs = emptyList(),
            )

        assertEquals(LegacyAnnualSelectionRules.BestFNRoute.OPTIONAL_I_THEN_OPTIONAL_H_THEN_G, result.route)
        assertEquals("target", result.selected?.id)
        assertEquals(1, result.qAttempts.size)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `empty q paths fall through to shuffled p fallback`() {
        val random = IdentityShuffleRandomSource()
        val pools = LegacyAnnualBestFSourceRules.GroupPools<String, String>(
            primary = emptyList(),
            secondary = emptyList(),
            tertiary = emptyList(),
        )
        val allClubs = listOf(club("r0", legacyO = 2, legacyR0 = true), club("fallback", legacyO = 2))

        val result =
            select(
                random = random,
                subjectOverall = 20,
                subjectO0 = false,
                currentP0 = 2,
                pools = pools,
                allClubs = allClubs,
            )

        assertEquals("fallback", result.selected?.id)
        assertEquals(1, result.qAttempts.size)
        assertEquals(listOf("fallback"), result.fallbackAttempt?.shuffledCandidates?.map { it.id })
        assertEquals(0L, random.draws)
    }

    @Test
    fun `mode two post shuffle predicate rejects p0 below four before accepting next candidate`() {
        val random = IdentityShuffleRandomSource()
        val g = group("g", listOf(club("p3", legacyO = 2, legacyP0 = 3), club("p4", legacyO = 2, legacyP0 = 4)))
        val pools = LegacyAnnualBestFSourceRules.GroupPools(primary = listOf(g), secondary = emptyList(), tertiary = emptyList())

        val result =
            select(
                random = random,
                mode = 2,
                subjectOverall = 20,
                subjectO0 = false,
                currentP0 = 2,
                pools = pools,
                allClubs = emptyList(),
            )

        assertEquals("p4", result.selected?.id)
        assertEquals(1L, random.draws)
    }

    private fun select(
        random: RandomSource,
        mode: Int = 0,
        subjectOverall: Int,
        subjectO0: Boolean,
        currentP0: Int,
        currentQ0: Boolean = false,
        pools: LegacyAnnualBestFSourceRules.GroupPools<String, String>,
        allClubs: List<LegacyAnnualBestFSourceRules.Club<String>>,
    ) =
        LegacyAnnualBestFExecutionRules.select(
            random = random,
            mode = mode,
            subjectOverall = subjectOverall,
            subjectPosition = 0,
            subjectO0 = subjectO0,
            currentClubId = "current",
            currentLegacyO = 2,
            currentLegacyJ = 0,
            currentLegacyP0 = currentP0,
            currentQ0 = currentQ0,
            pools = pools,
            allClubs = allClubs,
            positionCounts = { IntArray(5) },
        )

    private fun group(
        id: String,
        clubs: List<LegacyAnnualBestFSourceRules.Club<String>>,
    ) = LegacyAnnualBestFSourceRules.Group(id = id, legacyP = 0, legacyA0 = 5, clubs = clubs)

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

    private class IdentityShuffleRandomSource : RandomSource {
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            draws++
            return bound - 1
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
