package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyProceduralMaterializationRulesTest {
    @Test
    fun `market value is calculated before final star flag and hash rewrite`() {
        val random = QueueRandomSource(0, 1)
        val materialized = LegacyProceduralMaterializationRules.materialize(
            random = random,
            draft = draft(
                legacyN = 5,
                legacyB = true,
                legacyC = 18,
                legacyE = 3,
                legacyO = 50,
            ),
            target = target(clubLevel = 22, legacyJ = 0),
        )

        assertEquals(listOf(5, 3), random.bounds)
        assertEquals(15, materialized.overall)
        assertTrue(materialized.star)
        assertFalse(materialized.worldTop)
        assertEquals(8, materialized.legacyHash)
        assertEquals(152_280, materialized.marketValue)
        assertEquals(18, materialized.age)
        assertEquals(300L, materialized.durationDays)
    }

    @Test
    fun `non flagged path preserves draft hash and mutually exclusive RNG order`() {
        val random = QueueRandomSource(4, 0, 0, 0)
        val materialized = LegacyProceduralMaterializationRules.materialize(
            random = random,
            draft = draft(
                legacyN = 9,
                legacyB = false,
                legacyC = 17,
                legacyE = 4,
                legacyO = 60,
            ),
            target = target(clubLevel = 18, legacyJ = 1),
        )

        assertEquals(listOf(5, 10, 200, 300), random.bounds)
        assertEquals(20, materialized.overall)
        assertFalse(materialized.star)
        assertFalse(materialized.worldTop)
        assertEquals(9, materialized.legacyHash)
        assertEquals(0, materialized.status)
        assertEquals(1, materialized.side)
    }

    @Test
    fun `procedural identity is deterministic and career scoped`() {
        assertEquals(
            "career:career-a:procedural:42",
            LegacyProceduralMaterializationRules.deterministicPlayerId("career-a", 42L),
        )
        assertEquals(
            "career:career-b:procedural:42",
            LegacyProceduralMaterializationRules.deterministicPlayerId("career-b", 42L),
        )
    }

    private fun draft(
        legacyN: Int,
        legacyB: Boolean,
        legacyC: Int,
        legacyE: Int,
        legacyO: Int,
    ) = LegacyProceduralPlayerRules.Draft(
        legacyN = legacyN,
        legacyB = legacyB,
        legacyC = legacyC,
        legacyE = legacyE,
        legacyJ = 4,
        legacyL = 11,
        legacyD = 29,
        name = "Procedural",
        legacyG = 1,
        legacyF = 20,
        legacyO = legacyO,
        legacyM = 6,
    )

    private fun target(clubLevel: Int, legacyJ: Int) =
        LegacyProceduralMaterializationRules.TargetContext(
            legacyR0 = false,
            legacyO = 0,
            legacyP0 = 4,
            legacyJ = legacyJ,
            clubLevel = clubLevel,
            currentYear = 2026,
        )

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val values = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = values.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
