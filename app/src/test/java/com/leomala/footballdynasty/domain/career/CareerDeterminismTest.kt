package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerDeterminismTest {
    private val engine = CareerSimulationEngine()

    @Test
    fun `same state and seed produce identical fingerprints`() {
        val first = CareerStateFactory.create("determinism", 12345L)
        val second = CareerStateFactory.create("determinism", 12345L)
        val differentSeed = CareerStateFactory.create("determinism", 54321L)

        assertEquals(CareerFingerprint.of(first), CareerFingerprint.of(second))
        assertNotEquals(CareerFingerprint.of(first), CareerFingerprint.of(differentSeed))
    }

    @Test
    fun `365 explicit temporal commands are reproducible`() {
        fun run(): CareerState {
            var state = CareerStateFactory.create("long-sequence", 777L)
            repeat(361) {
                state = engine.apply(state, CareerCommand.AdvanceOneDay).state
            }
            state = engine.apply(state, CareerCommand.TransitionSeason).state
            repeat(3) {
                state = engine.apply(state, CareerCommand.AdvanceOneDay).state
            }
            return state
        }

        val started = System.nanoTime()
        val first = run()
        val elapsed = System.nanoTime() - started
        val second = run()

        assertEquals(first, second)
        assertEquals(CareerFingerprint.of(first), CareerFingerprint.of(second))
        assertEquals(2, first.season.number)
        assertEquals(2027, first.season.year)
        assertEquals(GameDate(2027, 1, 6), LegacyCalendarRules.dateAt(first.calendar))
        assertEquals(1L, first.transitionCount)
        assertTrue(elapsed > 0L)
        println("PHASE4_CORE_BENCHMARK commands=365 elapsedNanos=$elapsed fingerprint=${CareerFingerprint.of(first)}")
    }

    @Test
    fun `checkpoint fingerprints expose every state transition`() {
        val initial = CareerStateFactory.create("checkpoint", 11L)
        val transition = engine.apply(initial, CareerCommand.AdvanceOneDay)
        assertEquals(CareerFingerprint.of(initial), transition.checkpoint.beforeFingerprint)
        assertEquals(CareerFingerprint.of(transition.state), transition.checkpoint.afterFingerprint)
        assertNotEquals(transition.checkpoint.beforeFingerprint, transition.checkpoint.afterFingerprint)
    }
}
