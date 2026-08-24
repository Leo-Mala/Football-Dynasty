package com.leomala.footballdynasty.domain.career

/**
 * Pure structural rules recovered from `best.a0.f() -> best.c0.n() -> best.f.e()/h()`.
 *
 * The legacy symbols are intentionally not renamed into business concepts beyond what their
 * observable roster behavior proves.
 */
object LegacyAnnualSquadFloorRules {
    private val targetMinimumCounts = intArrayOf(2, 3, 3, 5, 3)
    private val donorMinimumCounts = intArrayOf(3, 4, 4, 6, 4)

    data class DivisionRange(
        val minP0: Int,
        val maxP0: Int,
    )

    /** `best.a0.f()` invokes `c0.n()` only for entries where Q0 is false. */
    fun annualClubEligible(q0: Boolean): Boolean = !q0

    /** `c0.n()` performs at most one replenishment attempt for each deficient position 0..4. */
    fun deficientPositions(positionCounts: IntArray): List<Int> =
        (0..4).filter { positionCounts.getOrElse(it) { 0 } < targetMinimumCounts[it] }

    /** Overall argument passed by `c0.n()` to `f.e()`. */
    fun requestedOverall(
        targetR0: Boolean,
        targetO: Int,
        targetP0: Int,
    ): Int = LegacyAnnualSelectionRules.legacyMinimumOverall(targetR0, targetO, targetP0)

    /** Candidate-club P0 window used by `f.e()`. */
    fun donorDivisionRange(targetR0: Boolean, targetP0: Int): DivisionRange =
        if (targetR0) {
            DivisionRange(
                minP0 = (targetP0 - 2).coerceAtLeast(0),
                maxP0 = targetP0 + 1,
            )
        } else {
            DivisionRange(
                minP0 = (targetP0 - 1).coerceAtLeast(0),
                maxP0 = targetP0 + 1,
            )
        }

    /**
     * Structural donor-club filter. For non-R0 targets the legacy global-pool path additionally
     * requires J() equality; the R0 path already receives a country-scoped `best.x.T0()` pool.
     */
    fun donorClubEligible(
        targetR0: Boolean,
        targetP0: Int,
        targetJ: Int,
        donorP0: Int,
        donorJ: Int,
        donorQ0: Boolean,
        isTarget: Boolean,
    ): Boolean {
        if (donorQ0 || isTarget) return false
        val range = donorDivisionRange(targetR0, targetP0)
        if (donorP0 !in range.minP0..range.maxP0) return false
        return targetR0 || donorJ == targetJ
    }

    /** Player filter inside `best.f.h(...)`, before the donor-surplus check. */
    fun donorPlayerEligible(
        playerPosition: Int,
        requestedPosition: Int,
        playerOverall: Int,
        requestedOverall: Int,
        playerO0: Boolean,
        playerW0: Boolean,
    ): Boolean {
        val minOverall = (requestedOverall - 5).coerceAtLeast(5)
        val maxOverall = (requestedOverall + 5).coerceAtMost(100)
        return playerPosition == requestedPosition &&
            playerOverall in minOverall..maxOverall &&
            !playerO0 &&
            !playerW0
    }

    /** A donor is only allowed to lose a selected player when its position count meets this floor. */
    fun donorHasSafeSurplus(position: Int, donorPositionCount: Int): Boolean =
        position in 0..4 && donorPositionCount >= donorMinimumCounts[position]
}
