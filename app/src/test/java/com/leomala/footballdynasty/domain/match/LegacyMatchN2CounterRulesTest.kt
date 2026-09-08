package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchN2CounterRulesTest {
    @Test
    fun `fresh legacy n2 state exposes eleven zero getters`() {
        assertEquals(
            LegacyMatchN2CounterRules.GetterSnapshot(
                legacyA = 0,
                legacyB = 0,
                legacyC = 0,
                legacyD = 0,
                legacyE = 0,
                legacyF = 0,
                legacyG = 0,
                legacyH = 0,
                legacyI = 0,
                legacyJ = 0,
                legacyK = 0,
            ),
            LegacyMatchN2CounterRules.snapshot(LegacyMatchN2CounterRules.State()),
        )
    }

    @Test
    fun `legacy getter letters preserve exact non field order`() {
        val state = LegacyMatchN2CounterRules.State(
            rawA = 1,
            rawB = 2,
            rawC = 3,
            rawD = 4,
            rawE = 5,
            rawF = 6,
            rawG = 7,
            rawH = 8,
            rawI = 9,
            rawJ = 10,
            rawK = 11,
        )

        assertEquals(
            LegacyMatchN2CounterRules.GetterSnapshot(
                legacyA = 10,
                legacyB = 8,
                legacyC = 9,
                legacyD = 1,
                legacyE = 2,
                legacyF = 3,
                legacyG = 4,
                legacyH = 5,
                legacyI = 6,
                legacyJ = 11,
                legacyK = 7,
            ),
            LegacyMatchN2CounterRules.snapshot(state),
        )
    }

    @Test
    fun `all single field mutators preserve exact legacy targets`() {
        val result = LegacyMatchN2CounterRules.applyAll(
            LegacyMatchN2CounterRules.State(),
            listOf(
                LegacyMatchN2CounterRules.Mutation.LEGACY_L,
                LegacyMatchN2CounterRules.Mutation.LEGACY_M,
                LegacyMatchN2CounterRules.Mutation.LEGACY_N,
                LegacyMatchN2CounterRules.Mutation.LEGACY_O,
                LegacyMatchN2CounterRules.Mutation.LEGACY_P,
                LegacyMatchN2CounterRules.Mutation.LEGACY_Q,
                LegacyMatchN2CounterRules.Mutation.LEGACY_R,
                LegacyMatchN2CounterRules.Mutation.LEGACY_T,
                LegacyMatchN2CounterRules.Mutation.LEGACY_U,
                LegacyMatchN2CounterRules.Mutation.LEGACY_V,
            ),
        )

        assertEquals(
            LegacyMatchN2CounterRules.State(
                rawA = 1,
                rawB = 1,
                rawC = 1,
                rawD = 1,
                rawE = 0,
                rawF = 1,
                rawG = 1,
                rawH = 1,
                rawI = 1,
                rawJ = 1,
                rawK = 1,
            ),
            result,
        )
    }

    @Test
    fun `legacy s mutator increments raw e and raw a together`() {
        val initial = LegacyMatchN2CounterRules.State(rawA = 4, rawE = 7, rawK = 9)

        assertEquals(
            LegacyMatchN2CounterRules.State(rawA = 5, rawE = 8, rawK = 9),
            LegacyMatchN2CounterRules.apply(initial, LegacyMatchN2CounterRules.Mutation.LEGACY_S),
        )
    }

    @Test
    fun `repeated mutators accumulate without resetting unrelated fields`() {
        val result = LegacyMatchN2CounterRules.applyAll(
            LegacyMatchN2CounterRules.State(rawC = 3),
            listOf(
                LegacyMatchN2CounterRules.Mutation.LEGACY_M,
                LegacyMatchN2CounterRules.Mutation.LEGACY_M,
                LegacyMatchN2CounterRules.Mutation.LEGACY_N,
                LegacyMatchN2CounterRules.Mutation.LEGACY_S,
                LegacyMatchN2CounterRules.Mutation.LEGACY_S,
            ),
        )

        assertEquals(2, result.rawH)
        assertEquals(1, result.rawI)
        assertEquals(2, result.rawA)
        assertEquals(2, result.rawE)
        assertEquals(3, result.rawC)
    }
}
