package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyTournamentBootstrapExecutionRulesTest {
    private data class Club(val id: String, val rawJ: Int, val rawJ0: Int = 0)

    @Test
    fun `complete prior slots consume only permutation draw and freeze exact a0 f0 arguments`() {
        val random = FixedIntRandomSource(1)
        val e = Club("e", 0)
        val f = Club("f", 0)
        val h = Club("h", 3)
        val g = Club("g", 2)
        val j = Club("j", 4)
        val i = Club("i", 5)

        val result =
            LegacyTournamentBootstrapExecutionRules.execute(
                random = random,
                y0 = present(e),
                v0 = present(f),
                x0 = present(h),
                w0 = present(g),
                z0 = present(j),
                b0 = present(i),
                fallbackSource = emptyList(),
                legacyK0E = 42,
                rawJ = { it.rawJ },
                rawJ0 = { it.rawJ0 },
            )

        val construction = requireNotNull(result.construction)
        assertEquals(1, construction.legacyNextInt3)
        assertEquals(listOf("j", "i", "h", "e"), construction.participants.map { it.id })
        assertEquals(4, construction.a0.participantCount)
        assertEquals(42, construction.a0.legacyK0E)
        assertEquals(1, construction.a0.rawFourthArgument)
        assertEquals(List(7) { false }, construction.a0.flags)
        assertEquals(150, construction.a0.rawLastArgument)
        assertEquals(0, construction.f0.rawIndex)
        assertEquals(false, construction.f0.rawBoolean1)
        assertEquals(0, construction.f0.rawInt1)
        assertEquals(0, construction.f0.rawInt2)
        assertEquals(42, construction.f0.legacyK0E)
        assertEquals(false, construction.f0.rawBoolean2)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `fallback shuffle fills raw J two through five before final permutation draw`() {
        val random = FixedIntRandomSource(3, 2, 1, 2)
        val absent = absent()
        val result =
            LegacyTournamentBootstrapExecutionRules.execute(
                random = random,
                y0 = present(Club("e", 0)),
                v0 = present(Club("f", 0)),
                x0 = absent,
                w0 = absent,
                z0 = absent,
                b0 = absent,
                fallbackSource =
                    listOf(
                        Club("j2", 2),
                        Club("j3", 3),
                        Club("j4", 4),
                        Club("j5", 5),
                    ),
                legacyK0E = 7,
                rawJ = { it.rawJ },
                rawJ0 = { it.rawJ0 },
            )

        assertEquals(listOf("j2", "j3", "j4", "j5"), result.shuffledFallbackCandidates.map { it.id })
        assertEquals("j2", result.tiers.tier2?.id)
        assertEquals("j3", result.tiers.tier3?.id)
        assertEquals("j4", result.tiers.tier4?.id)
        assertEquals("j5", result.tiers.tier5?.id)
        assertEquals(2, result.construction?.legacyNextInt3)
        assertEquals(listOf("e", "j5", "j4", "j3"), result.construction?.participants?.map { it.id })
        assertEquals(4L, random.draws)
    }

    @Test
    fun `incomplete slots never consume permutation draw or construct a0 f0`() {
        val random = FixedIntRandomSource()
        val absent = absent()
        val result =
            LegacyTournamentBootstrapExecutionRules.execute(
                random = random,
                y0 = present(Club("e", 0)),
                v0 = present(Club("f", 0)),
                x0 = absent,
                w0 = absent,
                z0 = absent,
                b0 = absent,
                fallbackSource = listOf(Club("j2", 2)),
                legacyK0E = 1,
                rawJ = { it.rawJ },
                rawJ0 = { it.rawJ0 },
            )

        assertNull(result.construction)
        assertEquals(0L, random.draws)
    }

    private fun present(club: Club) =
        LegacyTournamentBootstrapRules.PriorCompetition(present = true, first = club)

    private fun absent() =
        LegacyTournamentBootstrapRules.PriorCompetition<Club>(present = false, first = null)

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
