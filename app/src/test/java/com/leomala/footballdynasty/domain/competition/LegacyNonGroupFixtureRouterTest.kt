package com.leomala.footballdynasty.domain.competition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyNonGroupFixtureRouterTest {
    private fun input(
        teamCount: Int,
        legacyCompetitionType: Int = 0,
        legacyE0Flag: Boolean = false,
        requestedReverseCycle: Boolean = false,
        legacyGroupCount: Int = 1,
        legacyGlobalH0: Int = 0,
    ) = LegacyNonGroupFixtureRouter.Input(
        teamCount = teamCount,
        legacyCompetitionType = legacyCompetitionType,
        legacyE0Flag = legacyE0Flag,
        requestedReverseCycle = requestedReverseCycle,
        legacyGroupCount = legacyGroupCount,
        legacyGlobalH0 = legacyGlobalH0,
    )

    @Test
    fun `konrent t X non group mode preserves fixed team count routes`() {
        assertEquals(
            LegacyNonGroupFixtureRouter.Strategy.LEGACY_G_THREE,
            LegacyNonGroupFixtureRouter.decide(input(teamCount = 3)).strategy,
        )
        assertEquals(
            LegacyNonGroupFixtureRouter.Strategy.LEGACY_I_FIVE,
            LegacyNonGroupFixtureRouter.decide(input(teamCount = 5, legacyE0Flag = false)).strategy,
        )
        assertEquals(
            LegacyNonGroupFixtureRouter.Strategy.LEGACY_H_FIVE,
            LegacyNonGroupFixtureRouter.decide(input(teamCount = 5, legacyE0Flag = true)).strategy,
        )
        assertEquals(
            LegacyNonGroupFixtureRouter.Strategy.LEGACY_J_NINE_DOUBLE,
            LegacyNonGroupFixtureRouter.decide(input(teamCount = 9)).strategy,
        )
        assertEquals(
            LegacyNonGroupFixtureRouter.Strategy.LEGACY_E_NINETEEN,
            LegacyNonGroupFixtureRouter.decide(input(teamCount = 19)).strategy,
        )
        assertEquals(
            LegacyNonGroupFixtureRouter.Strategy.LEGACY_F_TWENTY_FIVE,
            LegacyNonGroupFixtureRouter.decide(input(teamCount = 25)).strategy,
        )
    }

    @Test
    fun `legacy type one overrides reverse cycle except 30 and 26 clubs`() {
        val ordinary = LegacyNonGroupFixtureRouter.decide(
            input(teamCount = 8, legacyCompetitionType = 1, requestedReverseCycle = false),
        )
        assertEquals(LegacyNonGroupFixtureRouter.Strategy.LEGACY_B_GENERIC, ordinary.strategy)
        assertEquals(2, ordinary.genericCycleCode)

        val thirty = LegacyNonGroupFixtureRouter.decide(
            input(teamCount = 30, legacyCompetitionType = 1, requestedReverseCycle = true),
        )
        assertEquals(1, thirty.genericCycleCode)

        val twentySix = LegacyNonGroupFixtureRouter.decide(
            input(teamCount = 26, legacyCompetitionType = 1, requestedReverseCycle = true),
        )
        assertEquals(1, twentySix.genericCycleCode)
    }

    @Test
    fun `legacy global H0 twenty preserves special cycle counts`() {
        assertEquals(
            4,
            LegacyNonGroupFixtureRouter.decide(
                input(teamCount = 10, legacyCompetitionType = 1, legacyGlobalH0 = 20),
            ).genericCycleCode,
        )
        assertEquals(
            3,
            LegacyNonGroupFixtureRouter.decide(
                input(teamCount = 12, legacyCompetitionType = 1, legacyGlobalH0 = 20),
            ).genericCycleCode,
        )
        assertEquals(
            3,
            LegacyNonGroupFixtureRouter.decide(
                input(teamCount = 14, legacyCompetitionType = 1, legacyGlobalH0 = 20),
            ).genericCycleCode,
        )
    }

    @Test
    fun `legacy type one with no groups clears X flag only on generic route`() {
        val clear = LegacyNonGroupFixtureRouter.decide(
            input(teamCount = 8, legacyCompetitionType = 1, legacyGroupCount = 0),
        )
        assertTrue(clear.clearLegacyXFlag)

        val preserve = LegacyNonGroupFixtureRouter.decide(
            input(teamCount = 8, legacyCompetitionType = 1, legacyGroupCount = 1),
        )
        assertFalse(preserve.clearLegacyXFlag)
    }

    @Test
    fun `generic router delegates using decided cycle count`() {
        val clubs = List(8) { index -> "club-${index + 1}" }
        val route = input(teamCount = clubs.size, requestedReverseCycle = true)
        val expected = LegacyLeagueFixtureRules.generate(clubs, legacyCycleCode = 2)
        assertEquals(expected, LegacyNonGroupFixtureRouter.generate(clubs, route))
    }
}
