package com.leomala.footballdynasty.domain.career

/**
 * Pure numeric composition of reachable legacy `best.c0.q()`.
 *
 * Executable SMALI proves that:
 * - senior `best.o.m0()` returns the raw `best.o.n:I` field directly;
 * - junior `best.p.u()` returns the raw `best.p.i:I` field directly;
 * - `best.c0.q()` widens each returned int to JVM long and accumulates every senior first,
 *   followed by every junior.
 *
 * These helpers freeze that scalar mapping only. They do not invent a modern durable owner for
 * senior `best.o.n`; junior `best.p.i` is already represented by V14 `CareerJuniorDraftEntity.legacyI`.
 */
object LegacyAnnualClubPayrollCompositionRules {
    data class Result(
        val seniorTotal: Long,
        val juniorTotal: Long,
        val total: Long,
    )

    fun seniorContributionFromLegacyN(legacyN: Int): Long = legacyN.toLong()

    fun juniorContributionFromLegacyI(legacyI: Int): Long = legacyI.toLong()

    fun composeFromRawFields(
        seniorLegacyN: List<Int>,
        juniorLegacyI: List<Int>,
    ): Result = compose(
        seniorContributions = seniorLegacyN.map(::seniorContributionFromLegacyN),
        juniorContributions = juniorLegacyI.map(::juniorContributionFromLegacyI),
    )

    fun compose(
        seniorContributions: List<Long>,
        juniorContributions: List<Long>,
    ): Result {
        var seniorTotal = 0L
        var total = 0L
        seniorContributions.forEach { contribution ->
            seniorTotal += contribution
            total += contribution
        }

        var juniorTotal = 0L
        juniorContributions.forEach { contribution ->
            juniorTotal += contribution
            total += contribution
        }

        return Result(
            seniorTotal = seniorTotal,
            juniorTotal = juniorTotal,
            total = total,
        )
    }
}
