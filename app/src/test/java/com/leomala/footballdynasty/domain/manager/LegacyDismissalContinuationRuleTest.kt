package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyDismissalContinuationRuleTest {
    private class FakeRandom(private val values: ArrayDeque<Int>) : RandomSource {
        override var draws: Long = 0
            private set
        val bounds = mutableListOf<Int>()

        override fun nextInt(bound: Int): Int {
            bounds += bound
            draws += 1
            val value = values.removeFirst()
            require(value in 0 until bound)
            return value
        }

        override fun nextBoolean(): Boolean = error("unused")
        override fun nextDouble(): Double = error("unused")
    }

    @Test
    fun `dismissal dispatch opens for human manager and preserves reset-load-open order`() {
        val effects = mutableListOf<String>()
        val result = LegacyDismissalDispatchRule.execute(
            currentCompetitionKind = { 1 },
            loadDismissals = { kind -> effects += "load:$kind"; listOf("cpu", "human") },
            managerOf = { it },
            isHumanManager = { it == "human" },
            worldV0 = { false },
            worldU1 = { 0 },
            resetDismissals = { effects += "reset" },
            openDismissals = { effects += "open:${it.size}" },
            dispatchPostDismissal = { effects += "post" },
        )

        assertTrue(result.openedDismissals)
        assertEquals(listOf("cpu", "human"), result.dismissals)
        assertEquals(listOf("reset", "load:1", "open:2"), effects)
        assertEquals(
            listOf(
                LegacyDismissalDispatchEffect.RESET_DISMISSALS,
                LegacyDismissalDispatchEffect.LOAD_DISMISSALS,
                LegacyDismissalDispatchEffect.OPEN_DISMISSALS,
            ),
            result.effectsInOrder,
        )
    }

    @Test
    fun `dismissal dispatch special V0 u1 gate opens non-human list only when non-empty`() {
        var v0Reads = 0
        var u1Reads = 0
        var opened = 0
        val result = LegacyDismissalDispatchRule.execute(
            currentCompetitionKind = { 3 },
            loadDismissals = { listOf("cpu") },
            managerOf = { it },
            isHumanManager = { false },
            worldV0 = { v0Reads += 1; true },
            worldU1 = { u1Reads += 1; 1 },
            resetDismissals = {},
            openDismissals = { opened += 1 },
            dispatchPostDismissal = { error("must not continue") },
        )

        assertTrue(result.openedDismissals)
        assertEquals(1, opened)
        assertEquals(1, v0Reads)
        assertEquals(1, u1Reads)
    }

    @Test
    fun `empty or unsupported dismissal source falls through without evaluating special gate`() {
        var specialReads = 0
        var post = 0
        val result = LegacyDismissalDispatchRule.execute<String, String>(
            currentCompetitionKind = { 2 },
            loadDismissals = { error("unsupported competition kind must not load") },
            managerOf = { it },
            isHumanManager = { false },
            worldV0 = { specialReads += 1; true },
            worldU1 = { specialReads += 1; 1 },
            resetDismissals = {},
            openDismissals = { error("must not open") },
            dispatchPostDismissal = { post += 1 },
        )

        assertFalse(result.openedDismissals)
        assertEquals(null, result.dismissals)
        assertEquals(0, specialReads)
        assertEquals(1, post)
        assertEquals(
            listOf(
                LegacyDismissalDispatchEffect.RESET_DISMISSALS,
                LegacyDismissalDispatchEffect.DISPATCH_POST_DISMISSAL,
            ),
            result.effectsInOrder,
        )
    }

    @Test
    fun `post-dismissal draw 50 skips g4 swallows helper exceptions and opens end-year on V0 E1`() {
        val random = FakeRandom(ArrayDeque(listOf(50)))
        val calls = mutableListOf<String>()
        var f2 = false
        val result = LegacyPostDismissalContinuationRule.execute(
            random = random,
            p0 = { calls += "p0"; 0 },
            o0 = { listOf("d") },
            runD4 = { calls += "d4"; throw IllegalStateException("legacy helper failure") },
            runG4 = { calls += "g4" },
            g0 = { listOf("e") },
            runE4 = { calls += "e4"; throw IllegalArgumentException("legacy helper failure") },
            setJ2 = { calls += "j2:$it" },
            setF2 = { f2 = it; calls += "f2:$it" },
            worldV0 = { calls += "v0"; true },
            worldE1 = { calls += "e1"; true },
            runWorldF = { calls += "worldF" },
            dispatchContinuationI = { calls += "i" },
            openEndYear = { calls += "endYear" },
        )

        assertEquals(0, result.capturedP0)
        assertEquals(50, result.g4GateDraw)
        assertEquals(listOf(100), random.bounds)
        assertEquals(1L, random.draws)
        assertTrue(f2)
        assertFalse("strict >50 gate must not run at 50", calls.contains("g4"))
        assertEquals(
            listOf("p0", "d4", "e4", "j2:1", "f2:true", "v0", "e1", "worldF", "endYear"),
            calls,
        )
        assertTrue(result.effectsInOrder.contains(LegacyPostDismissalContinuationEffect.SWALLOW_D4_EXCEPTION))
        assertTrue(result.effectsInOrder.contains(LegacyPostDismissalContinuationEffect.SWALLOW_E4_EXCEPTION))
        assertFalse(result.effectsInOrder.contains(LegacyPostDismissalContinuationEffect.RUN_G4))
    }

    @Test
    fun `post-dismissal draw 51 runs g4 and V0 without E1 dispatches continuation`() {
        val random = FakeRandom(ArrayDeque(listOf(51)))
        val calls = mutableListOf<String>()
        val result = LegacyPostDismissalContinuationRule.execute(
            random = random,
            p0 = { 7 },
            o0 = { emptyList<Any>() },
            runD4 = { error("empty source") },
            runG4 = { calls += "g4" },
            g0 = { emptyList<Any>() },
            runE4 = { error("empty source") },
            setJ2 = { calls += "j2:$it" },
            setF2 = { calls += "f2:$it" },
            worldV0 = { true },
            worldE1 = { false },
            runWorldF = { calls += "worldF" },
            dispatchContinuationI = { calls += "i" },
            openEndYear = { calls += "endYear" },
        )

        assertEquals(7, result.capturedP0)
        assertEquals(51, result.g4GateDraw)
        assertEquals(listOf("g4", "j2:1", "i"), calls)
        assertTrue(result.effectsInOrder.contains(LegacyPostDismissalContinuationEffect.RUN_G4))
        assertFalse(result.effectsInOrder.contains(LegacyPostDismissalContinuationEffect.SET_F2_TRUE))
    }

    @Test
    fun `post-dismissal non-V0 E1 runs world finalizer without opening end-year`() {
        val random = FakeRandom(ArrayDeque(listOf(0)))
        val calls = mutableListOf<String>()
        LegacyPostDismissalContinuationRule.execute(
            random = random,
            p0 = { 1 },
            o0 = { null },
            runD4 = {},
            runG4 = {},
            g0 = { null },
            runE4 = {},
            setJ2 = {},
            setF2 = {},
            worldV0 = { false },
            worldE1 = { true },
            runWorldF = { calls += "worldF" },
            dispatchContinuationI = { calls += "i" },
            openEndYear = { calls += "endYear" },
        )
        assertEquals(listOf("worldF"), calls)
    }
}
