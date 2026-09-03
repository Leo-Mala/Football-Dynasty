package com.leomala.footballdynasty.domain.manager

/** Exact timestamp write of legacy `best.o.c(long, boolean)` from official Java + SMALI. */
object LegacyContractDateWriteRule {
    const val DAY_MILLIS: Long = 86_400_000L

    /**
     * Legacy behavior:
     * - start from the current career calendar timestamp;
     * - when [ignoreExistingContractAsBase] is false and the stored contract is later, use it;
     * - add [durationDays] * 86,400,000 using JVM Long arithmetic.
     *
     * No clamping or calendar-day normalization is introduced because the source method performs
     * a direct millisecond multiplication/addition.
     */
    fun endTimestampMillis(
        currentCareerTimestampMillis: Long,
        currentContractEndMillis: Long,
        durationDays: Long,
        ignoreExistingContractAsBase: Boolean,
    ): Long {
        var base = currentCareerTimestampMillis
        if (!ignoreExistingContractAsBase && currentContractEndMillis > base) {
            base = currentContractEndMillis
        }
        return base + (durationDays * DAY_MILLIS)
    }

    fun endTimestampMillis(
        currentCareerTimestampMillis: Long,
        currentContractEndMillis: Long,
        invocation: LegacyContractWriteInvocation,
    ): Long = endTimestampMillis(
        currentCareerTimestampMillis = currentCareerTimestampMillis,
        currentContractEndMillis = currentContractEndMillis,
        durationDays = invocation.durationDays.toLong(),
        ignoreExistingContractAsBase = invocation.booleanArgument,
    )
}
