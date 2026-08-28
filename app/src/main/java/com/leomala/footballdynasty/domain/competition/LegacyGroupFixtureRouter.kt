package com.leomala.footballdynasty.domain.competition

/** Exact group-mode routing recovered from legacy `konrent.t.X(..., groupMode=true, ...)`. */
object LegacyGroupFixtureRouter {
    enum class Strategy {
        LEGACY_K_FOUR_BY_FIVE,
        LEGACY_L_FOUR_BY_FOUR,
        LEGACY_C_TWO_BY_SIX,
        LEGACY_D_TWO_BY_EIGHT,
    }

    fun strategyFor(legacyV: Int): Strategy? = when (legacyV) {
        1 -> Strategy.LEGACY_K_FOUR_BY_FIVE
        11 -> Strategy.LEGACY_L_FOUR_BY_FOUR
        6, 0x70B -> Strategy.LEGACY_C_TWO_BY_SIX
        10 -> Strategy.LEGACY_D_TWO_BY_EIGHT
        else -> null
    }

    fun generate(
        legacyV: Int,
        groups: List<List<String>>,
    ): List<List<LegacyLeagueFixtureRules.Fixture>>? = when (strategyFor(legacyV)) {
        Strategy.LEGACY_K_FOUR_BY_FIVE -> LegacyGroupFixtureRules.legacyKFourGroupsOfFive(groups)
        Strategy.LEGACY_L_FOUR_BY_FOUR -> LegacyGroupFixtureRules.legacyLFourGroupsOfFour(groups)
        Strategy.LEGACY_C_TWO_BY_SIX -> LegacyGroupFixtureRules.legacyCTwoGroupsOfSix(groups)
        Strategy.LEGACY_D_TWO_BY_EIGHT -> LegacyGroupFixtureRules.legacyDTwoGroupsOfEight(groups)
        null -> null
    }
}
