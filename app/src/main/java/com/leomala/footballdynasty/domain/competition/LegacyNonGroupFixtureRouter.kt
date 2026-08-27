package com.leomala.footballdynasty.domain.competition

/**
 * Exact non-group routing recovered from legacy `konrent.t.X(..., groupMode=false, ...)`.
 * Field names remain neutral where their sporting meaning is not required by the routing proof.
 */
object LegacyNonGroupFixtureRouter {
    enum class Strategy {
        LEGACY_G_THREE,
        LEGACY_I_FIVE,
        LEGACY_J_NINE_DOUBLE,
        LEGACY_H_FIVE,
        LEGACY_E_NINETEEN,
        LEGACY_F_TWENTY_FIVE,
        LEGACY_B_GENERIC,
    }

    data class Input(
        val teamCount: Int,
        val legacyCompetitionType: Int,
        val legacyE0Flag: Boolean,
        val requestedReverseCycle: Boolean,
        val legacyGroupCount: Int,
        val legacyGlobalH0: Int,
    )

    data class Decision(
        val strategy: Strategy,
        val genericCycleCode: Int? = null,
        val clearLegacyXFlag: Boolean = false,
    )

    fun decide(input: Input): Decision {
        require(input.teamCount >= 0)

        when {
            input.teamCount == 3 -> return Decision(Strategy.LEGACY_G_THREE)
            input.teamCount == 5 && !input.legacyE0Flag -> return Decision(Strategy.LEGACY_I_FIVE)
            input.teamCount == 9 -> return Decision(Strategy.LEGACY_J_NINE_DOUBLE)
            input.teamCount == 5 -> return Decision(Strategy.LEGACY_H_FIVE)
            input.teamCount == 19 -> return Decision(Strategy.LEGACY_E_NINETEEN)
            input.teamCount == 25 -> return Decision(Strategy.LEGACY_F_TWENTY_FIVE)
        }

        val reverseCycle = if (input.legacyCompetitionType == 1) {
            input.teamCount != 30 && input.teamCount != 26
        } else {
            input.requestedReverseCycle
        }
        val clearLegacyX = input.legacyCompetitionType == 1 && input.legacyGroupCount == 0

        val cycles = if (input.legacyCompetitionType == 1 && input.legacyGlobalH0 == 20) {
            when (input.teamCount) {
                10 -> 4
                12, 14 -> 3
                else -> if (reverseCycle) 2 else 1
            }
        } else if (reverseCycle) {
            2
        } else {
            1
        }

        return Decision(
            strategy = Strategy.LEGACY_B_GENERIC,
            genericCycleCode = cycles,
            clearLegacyXFlag = clearLegacyX,
        )
    }

    fun generate(
        clubIds: List<String>,
        input: Input,
    ): List<List<LegacyLeagueFixtureRules.Fixture>> {
        require(clubIds.size == input.teamCount) { "Team count must match club ids" }
        val decision = decide(input)
        return when (decision.strategy) {
            Strategy.LEGACY_G_THREE -> LegacyFixedLeagueFixtureRules.legacyGThreeClubs(clubIds)
            Strategy.LEGACY_I_FIVE -> LegacyFixedLeagueFixtureRules.legacyIFiveClubs(clubIds)
            Strategy.LEGACY_J_NINE_DOUBLE ->
                LegacyFixedLeagueFixtureRules.legacyJNineClubs(clubIds, reverseSecondCycle = true)

            Strategy.LEGACY_H_FIVE -> LegacyFixedLeagueFixtureRules.legacyHFiveClubs(clubIds)
            Strategy.LEGACY_E_NINETEEN -> LegacyLargeFixedLeagueFixtureRules.legacyENineteenClubs(clubIds)
            Strategy.LEGACY_F_TWENTY_FIVE -> LegacyLargeFixedLeagueFixtureRules.legacyFTwentyFiveClubs(clubIds)
            Strategy.LEGACY_B_GENERIC -> LegacyLeagueFixtureRules.generate(
                clubIds,
                requireNotNull(decision.genericCycleCode),
            )
        }
    }
}
