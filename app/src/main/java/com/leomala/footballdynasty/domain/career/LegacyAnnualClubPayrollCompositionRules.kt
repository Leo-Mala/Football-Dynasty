package com.leomala.footballdynasty.domain.career

/**
 * Pure numeric composition of reachable legacy `best.c0.q()`.
 *
 * Executable SMALI proves that `q()` accumulates, as JVM `long`, every already-computed senior
 * `best.o.m0()` contribution followed by every junior `best.p.u()` contribution. This rule does
 * not map modern salary codes to those legacy contribution methods; that separate Phase 15
 * boundary remains evidence-gated.
 */
object LegacyAnnualClubPayrollCompositionRules {
    data class Result(
        val seniorTotal: Long,
        val juniorTotal: Long,
        val total: Long,
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
