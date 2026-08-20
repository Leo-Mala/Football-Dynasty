package com.leomala.footballdynasty.foundation.random

import java.util.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class StatefulJavaRandomSourceTest {
    @Test
    fun `stateful random matches java Random and resumes from exact state`() {
        val seed = 987654321L
        val expected = Random(seed)
        val actual = StatefulJavaRandomSource(seed)

        repeat(64) {
            assertEquals(expected.nextInt(97), actual.nextInt(97))
            assertEquals(expected.nextBoolean(), actual.nextBoolean())
            assertEquals(expected.nextDouble(), actual.nextDouble(), 0.0)
        }
        assertEquals(192L, actual.draws)

        val snapshot = actual.snapshot()
        val tail = List(20) { actual.nextInt(10_000) }
        val restored = StatefulJavaRandomSource.restore(snapshot)
        assertEquals(tail, List(20) { restored.nextInt(10_000) })
        assertEquals(actual.snapshot(), restored.snapshot())
    }

    @Test
    fun `different seeds produce different deterministic state`() {
        val left = StatefulJavaRandomSource(1L)
        val right = StatefulJavaRandomSource(2L)
        assertNotEquals(left.snapshot().internalState, right.snapshot().internalState)
        assertNotEquals(left.nextInt(1_000_000), right.nextInt(1_000_000))
    }
}
