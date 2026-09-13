package com.leomala.footballdynasty.application.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CareerLineupEligibilityProjectionTest {
    @Test
    fun `application projection resolves persisted Q0 false before contract clock`() {
        assertEquals(
            true,
            CareerLineupEligibilityProjection.resolveWithoutCareerClock(
                player = player(q0 = false),
                preparation = preparation(lineupModeFlag = false),
            )
        )
    }

    @Test
    fun `application projection keeps Q0 true unresolved without exact career clock`() {
        assertNull(
            CareerLineupEligibilityProjection.resolveWithoutCareerClock(
                player = player(q0 = true),
                preparation = preparation(lineupModeFlag = false),
            )
        )
    }

    @Test
    fun `application projection preserves M0 rejection even when later owners are unresolved`() {
        assertEquals(
            false,
            CareerLineupEligibilityProjection.resolveWithoutCareerClock(
                player = player(q0 = null, blockedByM0 = true),
                preparation = preparation(lineupModeFlag = null),
            )
        )
    }

    private fun player(
        q0: Boolean?,
        blockedByM0: Boolean = false,
    ) = CareerLineupInputCatalogStore.PlayerInput(
        playerId = "p",
        name = "p",
        positionCode = 0,
        sideCode = 0,
        subroleCode = 0,
        skill = 50,
        energy = 50,
        star = false,
        sourceOrdinal = 0,
        hasClubForPreparedMatch = if (q0 == null) null else true,
        clubActiveQ0ForPreparedMatch = q0,
        blockedByM0ForPreparedMatch = blockedByM0,
        excludedByCompetitionV0ForPreparedMatch = false,
    )

    private fun preparation(
        lineupModeFlag: Boolean?,
    ) = CareerLineupInputCatalogStore.MatchPreparation(
        nextPlayableDayIndex = 0,
        matchId = "m",
        homeClubId = "home",
        awayClubId = "away",
        managedSide = CareerLineupInputCatalogStore.ManagedMatchSide.HOME,
        homeSeniorRosterCount = 1,
        awaySeniorRosterCount = 1,
        competitionId = null,
        competitionRestrictionActive = false,
        competitionDisciplineOwnerResolved = true,
        lineupModeFlag = lineupModeFlag,
        homeTacticIndex = null,
        awayTacticIndex = null,
        homeSubstitutionsRemaining = null,
        awaySubstitutionsRemaining = null,
        homeLegacyModeFlag = null,
        awayLegacyModeFlag = null,
        blockers = emptySet(),
    )
}
