package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualTournamentBootstrapRoutingRulesTest {
    @Test
    fun `annual q dispatches tournament bootstrap exactly once`() {
        var calls = 0

        LegacyAnnualTournamentBootstrapRoutingRules.dispatch {
            calls += 1
        }

        assertEquals(1, calls)
    }
}
