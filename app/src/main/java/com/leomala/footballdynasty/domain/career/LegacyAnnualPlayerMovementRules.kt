package com.leomala.footballdynasty.domain.career

/**
 * Structural projection of the exact annual call shape
 * `best.o.T1(destination, A0(), false, false, false)`.
 *
 * Names that would imply unproven financial/business semantics are deliberately avoided. The plan
 * records only calls and mutations confirmed by Java + SMALI in the Brasfoot 2026/27 corpus.
 */
object LegacyAnnualPlayerMovementRules {
    const val LEGACY_DURATION_ARGUMENT: Long = 180L
    const val LEDGER_CODE: Int = 1

    data class AnnualT1Plan(
        val amount: Int,
        val activityMainTeamDirty: Boolean,
        val resetX: Boolean,
        val resetZ: Boolean,
        val leaveYUnchanged: Boolean,
        val secondaryCalculatedAmount: Int,
        val sourceBCode1Amount: Int?,
        val targetDCode1Amount: Int?,
        val legacyDurationArgument: Long,
        val clearSourceSpecialReferences: Boolean,
        val removeFromSource: Boolean,
        val addToTarget: Boolean,
        val setS1True: Boolean,
        val clearSourceE1: Boolean,
    )

    /**
     * Produces the exact observable plan for annual callers. `sourceManaged`/`targetManaged` are
     * projections of legacy `Q0()`. Because the annual call passes z2=false, the generic T1
     * secondary percentage calculation is unreachable and remains exactly zero.
     */
    fun annualT1Plan(
        sourceExists: Boolean,
        sourceManaged: Boolean,
        targetManaged: Boolean,
        amount: Int,
    ): AnnualT1Plan {
        require(amount >= 0) { "legacy annual T1 amount cannot be negative" }

        val sourceLedgerAmount =
            amount.takeIf { sourceExists && sourceManaged && it > 0 }
        val targetLedgerAmount =
            amount.takeIf { targetManaged && it > 0 }
        val bothManaged = sourceExists && sourceManaged && targetManaged

        return AnnualT1Plan(
            amount = amount,
            activityMainTeamDirty = (sourceExists && sourceManaged) || targetManaged,
            resetX = true,
            resetZ = true,
            leaveYUnchanged = true,
            secondaryCalculatedAmount = 0,
            sourceBCode1Amount = sourceLedgerAmount,
            targetDCode1Amount = targetLedgerAmount,
            legacyDurationArgument = LEGACY_DURATION_ARGUMENT,
            clearSourceSpecialReferences = sourceExists,
            removeFromSource = sourceExists,
            addToTarget = true,
            setS1True = bothManaged,
            clearSourceE1 = bothManaged,
        )
    }
}
