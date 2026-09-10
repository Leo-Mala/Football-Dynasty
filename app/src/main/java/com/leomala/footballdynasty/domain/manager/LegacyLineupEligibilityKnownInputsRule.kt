package com.leomala.footballdynasty.domain.manager

/**
 * Partial evaluator for the recovered `best.o.K0(...)` predicate when the exact career clock is
 * not durably available yet.
 *
 * Nullable inputs here mean "owner unresolved", not an invented legacy value. The evaluator only
 * returns a Boolean when K0's own short-circuit order proves that the missing clock (or another
 * unresolved later operand) cannot affect the result.
 */
object LegacyLineupEligibilityKnownInputsRule {
    fun resolveWithoutCareerClock(
        blockedByM0: Boolean?,
        competitionRestrictionActive: Boolean?,
        excludedByCompetitionV0: Boolean?,
        lineupModeFlag: Boolean?,
        hasClub: Boolean?,
        clubActiveQ0: Boolean?,
    ): Boolean? {
        // K0 returns false immediately when M0() is true.
        if (blockedByM0 == true) return false
        if (blockedByM0 == null) return null

        val eligibleBeforeContractBranch = when (competitionRestrictionActive) {
            false -> true
            true -> when (excludedByCompetitionV0) {
                true -> false
                false -> true
                null -> return null
            }
            null -> return null
        }

        // `z2 || ...` is the first term of K0's final OR. Once true, no u0/Q0/K/clock read can
        // change the result.
        if (lineupModeFlag == true) return eligibleBeforeContractBranch
        if (lineupModeFlag == null) return null

        // With z2=false, the legacy OR next short-circuits on u0()==null and Q0()==null/false.
        // Persisted nulls are unresolved owners, so only proven false values may short-circuit.
        if (hasClub == false) return eligibleBeforeContractBranch
        if (hasClub == null) return null
        if (clubActiveQ0 == false) return eligibleBeforeContractBranch
        if (clubActiveQ0 == null) return null

        // z2=false, u0 present and Q0=true reaches `K >= currentCareerTime`; exact clock required.
        return null
    }
}
