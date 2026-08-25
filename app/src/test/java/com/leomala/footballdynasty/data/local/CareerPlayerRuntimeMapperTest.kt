package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import com.leomala.footballdynasty.domain.career.LegacyProceduralMaterializationRules
import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerPlayerRuntimeMapperTest {
    @Test
    fun `canonical mapping reproduces q1 value contract and loaded age normalization`() {
        val random = QueueRandomSource(2, 1, 0)
        val bundle = CareerPlayerRuntimeMapper.canonical(
            random = random,
            careerId = "career-a",
            player = PlayerEntity(
                id = "canonical-player",
                dataVersion = 1,
                importScope = "official",
                name = "Canonical",
                age = 15,
                country = 29,
                position = 3,
                status = 1,
                side = 1,
                cr1 = 4,
                cr2 = 11,
                star = false,
                worldTop = false,
                legacyAid = 0,
                legacySid = 0,
                legacyTid = 0,
                legacyHash = 5,
            ),
            target = target(currentGameEpochMillis = 1_000L),
        )

        assertEquals(listOf(3, 2, 30), random.bounds)
        assertEquals(CareerPlayerRuntimeStore.SOURCE_CANONICAL, bundle.runtime.sourceType)
        assertEquals(35, bundle.runtime.age)
        assertEquals(51, bundle.runtime.overall)
        assertEquals(6_970_680, bundle.runtime.marketValue)
        assertEquals(18_144_001_000L, bundle.runtime.contractEndEpochMillis)
        assertEquals("club-a", bundle.membership.clubId)
        assertFalse(bundle.runtime.legacyQ)
        assertFalse(bundle.runtime.legacyX)
        assertFalse(bundle.runtime.legacyY)
        assertFalse(bundle.runtime.legacyZ)
    }

    @Test
    fun `procedural mapping stores generated facts only in career scoped bundle`() {
        val bundle = CareerPlayerRuntimeMapper.procedural(
            careerId = "career-a",
            playerId = "career:career-a:procedural:42",
            materialized = LegacyProceduralMaterializationRules.Materialized(
                name = "Procedural",
                age = 18,
                country = 29,
                position = 3,
                status = 0,
                side = 1,
                cr1 = 4,
                cr2 = 11,
                star = true,
                worldTop = false,
                legacyHash = 8,
                overall = 15,
                marketValue = 152_280,
                legacyGeneratedO = 50,
                legacyCreatedYear = 2026,
                durationDays = 300L,
            ),
            target = target(currentGameEpochMillis = 2_000L),
        )

        assertEquals(CareerPlayerRuntimeStore.SOURCE_PROCEDURAL, bundle.runtime.sourceType)
        assertEquals("Procedural", bundle.procedural.name)
        assertEquals(29, bundle.procedural.country)
        assertEquals(15, bundle.runtime.overall)
        assertEquals(152_280, bundle.runtime.marketValue)
        assertEquals(25_920_002_000L, bundle.runtime.contractEndEpochMillis)
        assertEquals("club-a", bundle.membership.clubId)
        assertTrue(bundle.runtime.star)
        assertFalse(bundle.runtime.worldTop)
    }

    @Test
    fun `loaded age uses exact legacy u1 bounds`() {
        assertEquals(35, CareerPlayerRuntimeMapper.normalizeLoadedAge(15))
        assertEquals(16, CareerPlayerRuntimeMapper.normalizeLoadedAge(16))
        assertEquals(48, CareerPlayerRuntimeMapper.normalizeLoadedAge(48))
        assertEquals(35, CareerPlayerRuntimeMapper.normalizeLoadedAge(49))
    }

    private fun target(currentGameEpochMillis: Long) = CareerPlayerRuntimeMapper.TargetContext(
        clubId = "club-a",
        legacyR0 = false,
        legacyO = 0,
        legacyP0 = 4,
        legacyF0 = 20,
        currentYear = 2026,
        currentGameEpochMillis = currentGameEpochMillis,
        rosterKind = "SENIOR",
        sourceOrdinal = 0,
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
