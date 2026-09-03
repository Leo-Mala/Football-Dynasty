package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the loan-management branch in
 * `DialogIgrokInfo.q()/i()/h()`.
 *
 * Proven legacy distinctions:
 * - `N0()` means the player is currently on loan and action code 1 cancels it;
 * - `M()` is the independent "listed for loan" flag;
 * - action code 2 clears that list flag;
 * - action code 3 sets that list flag;
 * - offering action 3 is blocked when `club.d0() + club.e0() >= 3`;
 * - an active loan takes precedence over the list flag and the count limit.
 *
 * `h()` finds the first global `components.o2` record whose player reference is
 * identical to the selected player, removes that first matching record through
 * `best.b.p2(player)`, calls `player.U1(storedSourceClub)`, marks
 * `ActivityMainTeam.F = true`, and finishes the activity. Identity matching is
 * deliberately supplied as an index by the compatibility layer instead of
 * being guessed from a player code.
 */
enum class LegacyLoanListMutation {
    UNCHANGED,
    SET_FALSE,
    SET_TRUE,
}

data class LegacyLoanMenuDecision(
    val actionCode: Int?,
    val blockedByClubLoanLimit: Boolean,
)

data class LegacyEarlyLoanReturnPlan(
    val matchingRecordIndex: Int?,
    val storedSourceClubCode: Int?,
    val removeMatchingRecord: Boolean,
    val invokeReturnMove: Boolean,
    val markMainTeamDirty: Boolean,
)

object LegacyLoanManagementRule {
    const val ACTION_CANCEL_CURRENT_LOAN: Int = 1
    const val ACTION_REMOVE_FROM_LOAN_LIST: Int = 2
    const val ACTION_ADD_TO_LOAN_LIST: Int = 3
    const val LEGACY_CLUB_LOAN_LIMIT: Int = 3

    fun menuDecision(
        currentlyOnLoan: Boolean,
        listedForLoan: Boolean,
        firstClubLoanCount: Int,
        secondClubLoanCount: Int,
    ): LegacyLoanMenuDecision {
        if (currentlyOnLoan) {
            return LegacyLoanMenuDecision(
                actionCode = ACTION_CANCEL_CURRENT_LOAN,
                blockedByClubLoanLimit = false,
            )
        }

        if (listedForLoan) {
            return LegacyLoanMenuDecision(
                actionCode = ACTION_REMOVE_FROM_LOAN_LIST,
                blockedByClubLoanLimit = false,
            )
        }

        if (firstClubLoanCount + secondClubLoanCount >= LEGACY_CLUB_LOAN_LIMIT) {
            return LegacyLoanMenuDecision(
                actionCode = null,
                blockedByClubLoanLimit = true,
            )
        }

        return LegacyLoanMenuDecision(
            actionCode = ACTION_ADD_TO_LOAN_LIST,
            blockedByClubLoanLimit = false,
        )
    }

    fun listMutation(actionCode: Int): LegacyLoanListMutation = when (actionCode) {
        ACTION_REMOVE_FROM_LOAN_LIST -> LegacyLoanListMutation.SET_FALSE
        ACTION_ADD_TO_LOAN_LIST -> LegacyLoanListMutation.SET_TRUE
        else -> LegacyLoanListMutation.UNCHANGED
    }

    /**
     * Reconstructs `DialogIgrokInfo.h()` after object-identity resolution has
     * located the first matching `components.o2` entry.
     *
     * `firstIdentityMatchIndex` is nullable because the legacy loop can fail to
     * find a record; `h()` still invokes `U1(null)` in that case. The modern
     * layer therefore preserves the invocation instead of inventing a fallback
     * club.
     */
    fun earlyReturnPlan(
        records: List<LegacyLoanRecord>,
        firstIdentityMatchIndex: Int?,
    ): LegacyEarlyLoanReturnPlan {
        val validIndex = firstIdentityMatchIndex?.takeIf { it in records.indices }
        val sourceClubCode = validIndex?.let { records[it].sourceClubCode }

        return LegacyEarlyLoanReturnPlan(
            matchingRecordIndex = validIndex,
            storedSourceClubCode = sourceClubCode,
            removeMatchingRecord = validIndex != null,
            invokeReturnMove = true,
            markMainTeamDirty = true,
        )
    }

    fun removeMatchingRecord(
        records: List<LegacyLoanRecord>,
        plan: LegacyEarlyLoanReturnPlan,
    ): List<LegacyLoanRecord> {
        val index = plan.matchingRecordIndex ?: return records
        if (index !in records.indices) return records
        return records.toMutableList().also { it.removeAt(index) }
    }
}
