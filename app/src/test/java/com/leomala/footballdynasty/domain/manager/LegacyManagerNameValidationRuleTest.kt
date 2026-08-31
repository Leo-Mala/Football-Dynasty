package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyManagerNameValidationRuleTest {
    @Test
    fun nullAndEmptyNamesFollowTheLegacyEmptyBranch() {
        assertEquals(
            LegacyManagerNameValidationResult.EMPTY,
            LegacyManagerNameValidationRule.evaluate(null),
        )
        assertEquals(
            LegacyManagerNameValidationResult.EMPTY,
            LegacyManagerNameValidationRule.evaluate(""),
        )
    }

    @Test
    fun oneCodeUnitIsTooShortAndTwoAreAccepted() {
        assertEquals(
            LegacyManagerNameValidationResult.TOO_SHORT,
            LegacyManagerNameValidationRule.evaluate("A"),
        )
        assertEquals(
            LegacyManagerNameValidationResult.ACCEPTED,
            LegacyManagerNameValidationRule.evaluate("AB"),
        )
    }

    @Test
    fun thirtyFiveCodeUnitsAreAcceptedAndThirtySixAreTooLong() {
        assertEquals(
            LegacyManagerNameValidationResult.ACCEPTED,
            LegacyManagerNameValidationRule.evaluate("A".repeat(35)),
        )
        assertEquals(
            LegacyManagerNameValidationResult.TOO_LONG,
            LegacyManagerNameValidationRule.evaluate("A".repeat(36)),
        )
    }

    @Test
    fun legacyValidationDoesNotTrimWhitespace() {
        assertEquals(
            LegacyManagerNameValidationResult.TOO_SHORT,
            LegacyManagerNameValidationRule.evaluate(" "),
        )
        assertEquals(
            LegacyManagerNameValidationResult.ACCEPTED,
            LegacyManagerNameValidationRule.evaluate("  "),
        )
        assertEquals(
            LegacyManagerNameValidationResult.ACCEPTED,
            LegacyManagerNameValidationRule.evaluate(" A"),
        )
    }
}
