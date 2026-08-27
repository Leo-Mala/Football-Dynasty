package com.leomala.footballdynasty.domain.match

/** Structural parity for legacy `best.s.P0()` recovered directly from SMALI. */
object LegacyMatchP0Rules {
    enum class Outcome {
        UNRESOLVED,
        LEGACY_SIDE_1,
        LEGACY_SIDE_2,
    }

    data class Input(
        val legacyCompetitionP0: Boolean,
        val legacyE0Enabled: Boolean,
        val legacyB0: Int,
        val legacyD0: Int,
        val legacyC0: Int,
        val legacyE0: Int,
    )

    fun resolve(input: Input): Outcome {
        var side1Wins = if (input.legacyB0 > input.legacyD0) 1 else 0
        var side2Wins = if (input.legacyD0 > input.legacyB0) 1 else 0

        var aggregate1 = input.legacyB0
        var aggregate2 = input.legacyD0
        var priorLegacyE0 = 0

        if (input.legacyE0Enabled) {
            priorLegacyE0 = input.legacyE0
            if (input.legacyE0 > input.legacyC0) {
                side1Wins++
            } else if (input.legacyC0 > input.legacyE0) {
                side2Wins++
            }
            aggregate1 = input.legacyB0 + input.legacyE0
            aggregate2 = input.legacyC0 + input.legacyD0
        }

        var outcome = when {
            side1Wins > side2Wins -> Outcome.LEGACY_SIDE_1
            side2Wins > side1Wins -> Outcome.LEGACY_SIDE_2
            else -> Outcome.UNRESOLVED
        }

        if (outcome == Outcome.UNRESOLVED) {
            outcome = when {
                aggregate1 > aggregate2 -> Outcome.LEGACY_SIDE_1
                aggregate2 > aggregate1 -> Outcome.LEGACY_SIDE_2
                else -> Outcome.UNRESOLVED
            }
        }

        if (
            outcome == Outcome.UNRESOLVED &&
            input.legacyE0Enabled &&
            input.legacyCompetitionP0
        ) {
            outcome = when {
                priorLegacyE0 > input.legacyD0 -> Outcome.LEGACY_SIDE_1
                input.legacyD0 > priorLegacyE0 -> Outcome.LEGACY_SIDE_2
                else -> Outcome.UNRESOLVED
            }
        }

        return outcome
    }

    fun isUnresolved(input: Input): Boolean = resolve(input) == Outcome.UNRESOLVED
}
