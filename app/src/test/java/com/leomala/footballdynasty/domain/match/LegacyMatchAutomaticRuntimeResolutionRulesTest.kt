package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchAutomaticRuntimeResolutionRulesTest {
    @Test
    fun `automatic runtime applies reachable O result then clears both club mode flags`() {
        val state = fixture(homeMode = true, awayMode = true)
        val result = LegacyMatchAutomaticRuntimeResolutionRules.run<String, String, String>(
            state = state,
            random = ZeroRandom(),
            homeTacticIndex = 0,
            awayTacticIndex = 0,
            initialCounters = LegacyMatchMinuteActionRules.Counters(0, 0, 0),
            advanceR3 = { _, _ -> null },
            halftimeTransition = { _, _ -> },
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { true },
        )

        assertTrue(result.postSimulation.invokeLegacyO)
        assertEquals(LegacyMatchPostGateORules.SelectedSide.LEGACY_E, result.legacyO?.selectedSide)
        assertEquals(listOf(2, 1), result.legacyO?.legacyD0Values)
        assertFalse(state.home.legacyModeFlag)
        assertFalse(state.away.legacyModeFlag)
    }

    @Test
    fun `short circuited post gate skips O but still clears both club mode flags`() {
        val state = fixture(homeMode = true, awayMode = true)
        var p0Calls = 0
        val result = LegacyMatchAutomaticRuntimeResolutionRules.run<String, String, String>(
            state = state,
            random = ZeroRandom(),
            homeTacticIndex = 0,
            awayTacticIndex = 0,
            initialCounters = LegacyMatchMinuteActionRules.Counters(0, 0, 0),
            advanceR3 = { _, _ -> null },
            halftimeTransition = { _, _ -> },
            legacyZFlag = false,
            legacyA0Flag = true,
            resolveP0 = { p0Calls++; true },
        )

        assertEquals(0, p0Calls)
        assertNull(result.legacyO)
        assertTrue(result.postSimulation.clearBothClubFlags)
        assertFalse(state.home.legacyModeFlag)
        assertFalse(state.away.legacyModeFlag)
    }

    private fun fixture(
        homeMode: Boolean,
        awayMode: Boolean,
    ): LegacyMatchTransientRuntime.State<String, String> {
        val homePlayer = LegacyMatchTransientRuntime.Player(
            value = "home-player",
            legacyG0 = 10,
            legacyL0 = 1,
            legacyF0 = 1,
            legacyR = 1,
            age = 25,
            energy = 50,
            skill = 80,
        )
        val home = LegacyMatchTransientRuntime.Club(
            value = "home",
            legacyClubId = 101,
            active = mutableListOf(homePlayer),
            bench = mutableListOf(),
            substitutionsRemaining = 0,
            legacyModeFlag = homeMode,
        )
        val away = LegacyMatchTransientRuntime.Club<String, String>(
            value = "away",
            legacyClubId = 202,
            active = mutableListOf(),
            bench = mutableListOf(),
            substitutionsRemaining = 0,
            legacyModeFlag = awayMode,
        )
        return LegacyMatchTransientRuntime.State(
            currentSeasonId = 2026,
            home = home,
            away = away,
        )
    }

    private class ZeroRandom : RandomSource {
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            draws++
            return 0
        }

        override fun nextBoolean(): Boolean {
            draws++
            return false
        }

        override fun nextDouble(): Double {
            draws++
            return 0.0
        }
    }
}
