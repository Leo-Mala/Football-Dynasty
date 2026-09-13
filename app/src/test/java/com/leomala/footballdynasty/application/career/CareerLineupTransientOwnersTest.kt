package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.match.LegacyMatchSubstitutionRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CareerLineupTransientOwnersTest {
    @Test
    fun `persisted q0 flags materialize exact legacy transient club inputs`() {
        val owners = requireNotNull(
            CareerLineupInputCatalogStore.resolveTransientClubOwners(
                homeLegacyModeFlag = true,
                awayLegacyModeFlag = false,
            )
        )

        assertEquals(LegacyMatchSubstitutionRules.INITIAL_SUBSTITUTIONS_PER_SIDE, owners.homeSubstitutionsRemaining)
        assertEquals(LegacyMatchSubstitutionRules.INITIAL_SUBSTITUTIONS_PER_SIDE, owners.awaySubstitutionsRemaining)
        assertEquals(5, owners.homeSubstitutionsRemaining)
        assertEquals(5, owners.awaySubstitutionsRemaining)
        assertEquals(true, owners.homeLegacyModeFlag)
        assertEquals(false, owners.awayLegacyModeFlag)
    }

    @Test
    fun `missing persisted q0 keeps transient club inputs fail closed`() {
        assertNull(
            CareerLineupInputCatalogStore.resolveTransientClubOwners(
                homeLegacyModeFlag = null,
                awayLegacyModeFlag = false,
            )
        )
        assertNull(
            CareerLineupInputCatalogStore.resolveTransientClubOwners(
                homeLegacyModeFlag = true,
                awayLegacyModeFlag = null,
            )
        )
    }
}
