package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchDisciplinaryRulesTest {
    @Test
    fun `first legacy C increments to one and creates yellow card`() {
        assertEquals(
            LegacyMatchDisciplinaryRules.LegacyCResult(1, LegacyMatchEventType.YELLOW_CARD),
            LegacyMatchDisciplinaryRules.applyLegacyC(0),
        )
    }

    @Test
    fun `second legacy C increments to exactly two and creates combined yellow red event`() {
        assertEquals(
            LegacyMatchDisciplinaryRules.LegacyCResult(2, LegacyMatchEventType.SECOND_YELLOW_RED),
            LegacyMatchDisciplinaryRules.applyLegacyC(1),
        )
    }

    @Test
    fun `legacy equality quirk does not use greater than or equal`() {
        assertEquals(
            LegacyMatchDisciplinaryRules.LegacyCResult(3, LegacyMatchEventType.YELLOW_CARD),
            LegacyMatchDisciplinaryRules.applyLegacyC(2),
        )
    }

    @Test
    fun `legacy D always creates direct red event type`() {
        assertEquals(
            LegacyMatchEventType.RED_CARD,
            LegacyMatchDisciplinaryRules.legacyDType(),
        )
    }
}
