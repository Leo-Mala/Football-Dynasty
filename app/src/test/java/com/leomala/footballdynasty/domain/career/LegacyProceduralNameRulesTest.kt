package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyProceduralNameRulesTest {
    @Test
    fun `empty name asset returns null without rng`() {
        val random = QueueRandomSource()
        assertNull(LegacyProceduralNameRules.generate(random, emptyList(), emptyList()))
        assertEquals(0L, random.draws)
    }

    @Test
    fun `single-word first name adds surname with exact zero-index rewrite`() {
        val random = QueueRandomSource(1, 0)
        val result = LegacyProceduralNameRules.generate(
            random,
            names = listOf("unused", "Ana", "Bia"),
            surnames = listOf("unused", "Silva", "Costa"),
        )
        assertEquals("Ana Silva", result)
        assertEquals(listOf(3, 3), random.bounds)
    }

    @Test
    fun `thousand-name bias consumes replacement draw only when gate is zero`() {
        val names = List(1000) { "Name$it" }
        val random = QueueRandomSource(900, 0, 12)
        val result = LegacyProceduralNameRules.generate(random, names, listOf("x", "y"))
        assertEquals("Name12", result)
        assertEquals(listOf(1000, 2, 500), random.bounds)
    }

    @Test
    fun `two-word name can append a short second name`() {
        val random = QueueRandomSource(1, 0, 2)
        val result = LegacyProceduralNameRules.generate(
            random,
            names = listOf("unused", "Ana Maria", "Jo"),
            surnames = emptyList(),
        )
        assertEquals("Ana Maria Jo", result)
        assertEquals(listOf(3, 2, 3), random.bounds)
    }

    @Test
    fun `two-word name skips second-name draw when combine gate fails`() {
        val random = QueueRandomSource(1, 1)
        val result = LegacyProceduralNameRules.generate(
            random,
            names = listOf("unused", "Ana Maria", "Jo"),
            surnames = emptyList(),
        )
        assertEquals("Ana Maria", result)
        assertEquals(listOf(3, 2), random.bounds)
    }

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
