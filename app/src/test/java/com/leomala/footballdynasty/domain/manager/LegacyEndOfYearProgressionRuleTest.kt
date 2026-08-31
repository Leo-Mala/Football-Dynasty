package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyEndOfYearProgressionRuleTest {
    private class Manager(val id: String, val human: Boolean)
    private class Row(val id: String, val manager: Manager?)

    private class RecordingRandomSource(
        private val value: Int,
        private val calls: MutableList<String>,
    ) : RandomSource {
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            calls += "nextInt:$bound"
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("unexpected nextBoolean")
        override fun nextDouble(): Double = error("unexpected nextDouble")
    }

    @Test
    fun unsupportedCompetitionKindSkipsRowsAndContinuesWithoutReadingSpecialFlags() {
        val calls = mutableListOf<String>()

        val result = LegacyDismissalGateRule.execute<Row, Manager>(
            currentCompetitionKind = { calls += "kind"; 2 },
            dismissalRowsForKind = { calls += "load:$it"; emptyList() },
            managerOfRow = { calls += "manager:${it.id}"; it.manager },
            isHumanManager = { calls += "human:${it.id}"; it.human },
            legacyV0 = { calls += "V0"; true },
            legacyU1 = { calls += "u1"; 1 },
            openDismissals = { calls += "open" },
            continueEndOfYear = { calls += "m" },
        )

        assertNull(result.dismissalRows)
        assertEquals(
            listOf(
                LegacyDismissalGateEffect.RESET_DISMISSAL_ROWS,
                LegacyDismissalGateEffect.CONTINUE_END_OF_YEAR,
            ),
            result.effectsInOrder,
        )
        assertEquals(listOf("kind", "m"), calls)
    }

    @Test
    fun humanDismissalRowUsesDoubleManagerLookupScansAllRowsThenOpens() {
        val human = Manager("human", human = true)
        val rows = listOf(Row("a", human), Row("b", null))
        val calls = mutableListOf<String>()

        val result = LegacyDismissalGateRule.execute<Row, Manager>(
            currentCompetitionKind = { calls += "kind"; 1 },
            dismissalRowsForKind = { calls += "load:$it"; rows },
            managerOfRow = { calls += "manager:${it.id}"; it.manager },
            isHumanManager = { calls += "human:${it.id}"; it.human },
            legacyV0 = { calls += "V0"; true },
            legacyU1 = { calls += "u1"; 0 },
            openDismissals = { calls += "open" },
            continueEndOfYear = { calls += "m" },
        )

        assertSame(rows, result.dismissalRows)
        assertEquals(
            listOf("kind", "load:1", "manager:a", "manager:a", "human:human", "manager:b", "V0", "u1", "open"),
            calls,
        )
        assertTrue(result.effectsInOrder.contains(LegacyDismissalGateEffect.OPEN_DISMISSALS))
        assertFalse(result.effectsInOrder.contains(LegacyDismissalGateEffect.CONTINUE_END_OF_YEAR))
    }

    @Test
    fun specialV0U1GateOpensEvenWithoutHumanManager() {
        val ai = Manager("ai", human = false)
        val rows = listOf(Row("row", ai))

        val result = LegacyDismissalGateRule.execute<Row, Manager>(
            currentCompetitionKind = { 3 },
            dismissalRowsForKind = { rows },
            managerOfRow = { it.manager },
            isHumanManager = { it.human },
            legacyV0 = { true },
            legacyU1 = { 1 },
            openDismissals = {},
            continueEndOfYear = { error("must open dismissals") },
        )

        assertTrue(result.effectsInOrder.contains(LegacyDismissalGateEffect.LOAD_DISMISSAL_ROWS))
        assertTrue(result.effectsInOrder.contains(LegacyDismissalGateEffect.OPEN_DISMISSALS))
    }

    @Test
    fun emptyDismissalRowsFallThroughWithoutReadingV0OrU1() {
        val calls = mutableListOf<String>()

        val result = LegacyDismissalGateRule.execute<Row, Manager>(
            currentCompetitionKind = { 1 },
            dismissalRowsForKind = { emptyList() },
            managerOfRow = { it.manager },
            isHumanManager = { it.human },
            legacyV0 = { calls += "V0"; true },
            legacyU1 = { calls += "u1"; 1 },
            openDismissals = { error("empty list must not open") },
            continueEndOfYear = { calls += "m" },
        )

        assertEquals(listOf("m"), calls)
        assertTrue(result.effectsInOrder.contains(LegacyDismissalGateEffect.CONTINUE_END_OF_YEAR))
    }

    @Test
    fun fullEndOfYearPathPreservesExceptionSwallowingSingleDrawAndFinalActivityOrder() {
        val calls = mutableListOf<String>()
        var oReads = 0
        var gReads = 0
        val random = RecordingRandomSource(51, calls)

        val result = LegacyEndOfYearProgressionRule.execute(
            currentP0 = { calls += "P0"; 0 },
            o0 = { calls += "o0:${++oReads}"; listOf("o") },
            d4 = { calls += "d4"; throw IllegalStateException("legacy swallow") },
            random = random,
            g4 = { calls += "g4" },
            g0 = { calls += "g0:${++gReads}"; listOf("g") },
            e4 = { calls += "e4"; throw IllegalArgumentException("legacy swallow") },
            j2 = { calls += "j2:$it" },
            f2 = { calls += "F2:$it" },
            legacyV0 = { calls += "V0"; true },
            e1 = { calls += "E1"; true },
            finalizeF = { calls += "F" },
            continueI = { calls += "i" },
            openEndYear = { calls += "ActivityFimAno" },
        )

        assertEquals(51, result.randomDraw)
        assertEquals(1L, random.draws)
        assertEquals(
            listOf(
                "P0",
                "o0:1",
                "o0:2",
                "d4",
                "nextInt:100",
                "g4",
                "g0:1",
                "g0:2",
                "e4",
                "j2:1",
                "F2:true",
                "V0",
                "E1",
                "F",
                "ActivityFimAno",
            ),
            calls,
        )
        assertEquals(
            listOf(
                LegacyEndOfYearProgressionEffect.CAPTURE_INITIAL_P0,
                LegacyEndOfYearProgressionEffect.TRY_D4,
                LegacyEndOfYearProgressionEffect.DRAW_G4_GATE,
                LegacyEndOfYearProgressionEffect.RUN_G4,
                LegacyEndOfYearProgressionEffect.TRY_E4,
                LegacyEndOfYearProgressionEffect.SET_J2_ONE,
                LegacyEndOfYearProgressionEffect.SET_F2_TRUE,
                LegacyEndOfYearProgressionEffect.FINALIZE_F,
                LegacyEndOfYearProgressionEffect.OPEN_END_YEAR,
            ),
            result.effectsInOrder,
        )
    }

    @Test
    fun drawFiftyDoesNotRunG4AndV0WithoutE1ContinuesThroughI() {
        val calls = mutableListOf<String>()
        val random = RecordingRandomSource(50, calls)

        val result = LegacyEndOfYearProgressionRule.execute(
            currentP0 = { 2 },
            o0 = { emptyList<Any>() },
            d4 = { error("empty o0") },
            random = random,
            g4 = { error("draw 50 must not call g4") },
            g0 = { emptyList<Any>() },
            e4 = { error("empty g0") },
            j2 = { calls += "j2:$it" },
            f2 = { error("P0 was not zero") },
            legacyV0 = { true },
            e1 = { false },
            finalizeF = { error("E1 false must not finalize") },
            continueI = { calls += "i" },
            openEndYear = { error("E1 false must not open") },
        )

        assertEquals(50, result.randomDraw)
        assertFalse(result.effectsInOrder.contains(LegacyEndOfYearProgressionEffect.RUN_G4))
        assertTrue(result.effectsInOrder.contains(LegacyEndOfYearProgressionEffect.CONTINUE_I))
        assertEquals(listOf("nextInt:100", "j2:1", "i"), calls)
    }

    @Test
    fun nonV0WithE1FinalizesWithoutOpeningEndYearActivity() {
        val calls = mutableListOf<String>()

        val result = LegacyEndOfYearProgressionRule.execute(
            currentP0 = { 1 },
            o0 = { null },
            d4 = {},
            random = RecordingRandomSource(0, calls),
            g4 = {},
            g0 = { null },
            e4 = {},
            j2 = {},
            f2 = {},
            legacyV0 = { false },
            e1 = { true },
            finalizeF = { calls += "F" },
            continueI = { calls += "i" },
            openEndYear = { calls += "fim" },
        )

        assertTrue(result.effectsInOrder.contains(LegacyEndOfYearProgressionEffect.FINALIZE_F))
        assertFalse(result.effectsInOrder.contains(LegacyEndOfYearProgressionEffect.CONTINUE_I))
        assertFalse(result.effectsInOrder.contains(LegacyEndOfYearProgressionEffect.OPEN_END_YEAR))
        assertEquals(listOf("nextInt:100", "F"), calls)
    }

    @Test(expected = NullPointerException::class)
    fun secondO0LookupReturningNullPreservesLegacyDereferenceBeforeRandomDraw() {
        val randomCalls = mutableListOf<String>()
        val random = RecordingRandomSource(99, randomCalls)
        var reads = 0

        try {
            LegacyEndOfYearProgressionRule.execute(
                currentP0 = { 1 },
                o0 = { if (++reads == 1) listOf("present") else null },
                d4 = {},
                random = random,
                g4 = {},
                g0 = { null },
                e4 = {},
                j2 = {},
                f2 = {},
                legacyV0 = { false },
                e1 = { false },
                finalizeF = {},
                continueI = {},
                openEndYear = {},
            )
        } finally {
            assertEquals(0L, random.draws)
            assertTrue(randomCalls.isEmpty())
        }
    }
}
