package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualEmploymentCandidateRulesTest {
    @Test
    fun `source collection preserves exact fixed source order and category map`() {
        val random = IdentityShuffleRandomSource()
        val result =
            LegacyAnnualEmploymentCandidateRules.select(
                random = random,
                input =
                    input(
                        legacyW = 0,
                        sources =
                            LegacyAnnualEmploymentCandidateRules.Sources(
                                t0 = listOf(club("t0", j = 2, p0 = 2), club("skip-q0", j = 2, p0 = 2, q0 = true)),
                                f0 = listOf(club("f0", j = 0, p0 = 1)),
                                r = listOf(club("r", j = 1, p0 = 1)),
                                q = emptyList(),
                                s = listOf(club("s", j = 3, p0 = 1)),
                                t = listOf(club("t", j = 5, p0 = 1)),
                                u = listOf(club("u", j = 4, p0 = 1)),
                            ),
                    ),
            )

        assertEquals(listOf("t0", "f0", "r", "s", "t", "u"), result.sourceCandidates.map { it.id })
        assertEquals(listOf(0, 1, 2, 3, 5, 4), result.sourceCategoryOrder)
        assertTrue(result.hasLegacyT0GroupPhase)
        assertEquals(5L, random.draws)
    }

    @Test
    fun `simple branch consumes default raw p0 matrix in candidate order`() {
        val result =
            LegacyAnnualEmploymentCandidateRules.select(
                random = IdentityShuffleRandomSource(),
                input =
                    input(
                        legacyW = 0,
                        sources =
                            LegacyAnnualEmploymentCandidateRules.Sources(
                                t0 =
                                    listOf(
                                        club("p1-a", j = 0, p0 = 1),
                                        club("p2", j = 1, p0 = 2),
                                        club("p1-b", j = 2, p0 = 1),
                                        club("p1-c", j = 3, p0 = 1),
                                    ),
                            ),
                    ),
            )

        assertEquals(listOf(2, 1, 1, -1, -1), result.requiredLegacyP0)
        assertEquals(listOf("p2", "p1-a", "p1-b"), result.selected.map { it.id })
    }

    @Test
    fun `indexed branch cycles source categories across two passes`() {
        val result =
            LegacyAnnualEmploymentCandidateRules.select(
                random = IdentityShuffleRandomSource(),
                input =
                    input(
                        legacyW = 0,
                        sources =
                            LegacyAnnualEmploymentCandidateRules.Sources(
                                f0 =
                                    listOf(
                                        club("j0-p2", j = 0, p0 = 2),
                                        club("j0-p1", j = 0, p0 = 1),
                                    ),
                                r =
                                    listOf(
                                        club("j1-p1-a", j = 1, p0 = 1),
                                        club("j1-p2", j = 1, p0 = 2),
                                        club("j1-p1-b", j = 1, p0 = 1),
                                    ),
                            ),
                    ),
            )

        assertEquals(listOf(0, 1), result.sourceCategoryOrder)
        assertEquals(
            listOf("j0-p2", "j1-p1-a", "j0-p1", "j1-p2", "j1-p1-b"),
            result.selected.map { it.id },
        )
    }

    @Test
    fun `late season A J zero preserves overwritten quota of seven for raw J zero`() {
        val rawJ0Clubs = (0 until 8).map { club("j0-$it", j = 0, p0 = 2) }
        val result =
            LegacyAnnualEmploymentCandidateRules.select(
                random = IdentityShuffleRandomSource(),
                input =
                    input(
                        legacyN0 = 10,
                        legacyB = 9,
                        legacyW = 1,
                        sourceAClubLegacyJ = 0,
                        sources =
                            LegacyAnnualEmploymentCandidateRules.Sources(
                                lateSeasonSources = listOf(rawJ0Clubs, null, null, null, null, null),
                            ),
                    ),
            )

        assertTrue(result.lateSeasonBranch)
        assertEquals(7, result.effectiveCandidates.size)
        assertEquals((0 until 7).map { "j0-$it" }, result.effectiveCandidates.map { it.id })
    }

    @Test
    fun `first H0 replacement preserves whole candidate list contains bug and last match overwrite`() {
        val result =
            LegacyAnnualEmploymentCandidateRules.select(
                random = IdentityShuffleRandomSource(),
                input =
                    input(
                        legacyW = 0,
                        firstH0LegacyU = 77,
                        firstH0AClubLegacyP0 = 2,
                        sources =
                            LegacyAnnualEmploymentCandidateRules.Sources(
                                t0 =
                                    listOf(
                                        club("p2", j = 0, p0 = 2),
                                        club("p1-a", j = 0, p0 = 1),
                                        club("p1-b", j = 0, p0 = 1),
                                        club("qualifies", j = 0, p0 = 9, j0 = 77, f0 = 10),
                                        club("later-nonqualifying", j = 0, p0 = 8, j0 = 77, f0 = 20),
                                    ),
                            ),
                    ),
            )

        assertEquals(listOf("p2", "p1-a", "later-nonqualifying"), result.selected.map { it.id })
    }

    @Test
    fun `empty source pool returns empty result and consumes no shuffle draws`() {
        val random = IdentityShuffleRandomSource()
        val result =
            LegacyAnnualEmploymentCandidateRules.select(
                random = random,
                input = input(legacyW = 0, sources = LegacyAnnualEmploymentCandidateRules.Sources()),
            )

        assertTrue(result.sourceCandidates.isEmpty())
        assertTrue(result.effectiveCandidates.isEmpty())
        assertTrue(result.selected.isEmpty())
        assertEquals(0L, random.draws)
    }

    private fun input(
        legacyN0: Int = 10,
        legacyB: Int = 0,
        legacyW: Int,
        resolvedLegacyPForU: Int = -1,
        sourceAClubLegacyJ: Int? = null,
        firstH0LegacyU: Int? = null,
        firstH0AClubLegacyP0: Int? = null,
        sources: LegacyAnnualEmploymentCandidateRules.Sources<String>,
    ) =
        LegacyAnnualEmploymentCandidateRules.Input(
            legacyN0 = legacyN0,
            legacyB = legacyB,
            legacyW = legacyW,
            resolvedLegacyPForU = resolvedLegacyPForU,
            sourceAClubLegacyJ = sourceAClubLegacyJ,
            firstH0LegacyU = firstH0LegacyU,
            firstH0AClubLegacyP0 = firstH0AClubLegacyP0,
            sources = sources,
        )

    private fun club(
        id: String,
        j: Int,
        p0: Int,
        j0: Int = -1,
        f0: Int = 99,
        q0: Boolean = false,
    ) =
        LegacyAnnualEmploymentCandidateRules.Club(
            id = id,
            legacyJ = j,
            legacyP0 = p0,
            legacyJ0 = j0,
            legacyF0 = f0,
            legacyQ0 = q0,
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
