package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource

data class LegacyReplacementManagerCandidate<T>(
    val value: T,
    val currentClubId: String?,
    val userControlled: Boolean,
    /** Legacy `best.f0.E()`. */
    val primaryCountryCode: Int,
    /** Legacy `best.f0.u()`. */
    val secondaryCountryCode: Int,
    /** Legacy `best.f0.w()`. */
    val levelCode: Int,
    /** Legacy comparator `best.b.L1`: `v()` descending. */
    val sortV: Int,
    /** Legacy comparator `best.b.L1`: `H()` descending. */
    val sortH: Int,
    /** Legacy comparator `best.b.L1`: `s()` ascending. */
    val sortS: Int,
)

data class LegacyReplacementTargetClub(
    /** Legacy `best.c0.j0()`. */
    val countryCode: Int,
    /** Legacy `best.c0.p0()`. */
    val levelCode: Int,
)

/**
 * Exact pure-state reconstruction of `best.b.t(c0,int)` and `best.b.u()` from the official
 * `com.brasfoot.v2020` corpus.
 *
 * The legacy `t` method allocates a fresh `Random` for the 0..99 sort/shuffle decision and
 * `Collections.shuffle` owns separate implicit randomness. The modern project has one explicit,
 * persistible [RandomSource]; therefore the same observable branch structure and Fisher-Yates
 * bounds are consumed sequentially from that source without claiming legacy seed equivalence.
 */
object LegacyReplacementManagerCandidateRule {
    fun modeBounds(mode: Int, targetLevel: Int): IntRange = when (mode) {
        -1 -> 0..5
        0 -> targetLevel..targetLevel
        1 -> (targetLevel - 1)..targetLevel
        // SMALI mode 2 writes lower=p0()-2 and upper=p0()+1, then falls directly into the filter
        // loop. The later p0() upper-bound write belongs only to the mode-0/mode-1 goto target.
        2 -> (targetLevel - 2)..(targetLevel + 1)
        // `t` is public. Unknown modes leave both loop-carried bound locals at their initial zero.
        else -> 0..0
    }

    fun <T> select(
        managers: List<LegacyReplacementManagerCandidate<T>>,
        targetClub: LegacyReplacementTargetClub,
        mode: Int,
        random: RandomSource,
    ): T? {
        val bounds = modeBounds(mode, targetClub.levelCode)
        val pool = managers.filterTo(mutableListOf()) { manager ->
            manager.currentClubId == null &&
                !manager.userControlled &&
                (manager.primaryCountryCode == targetClub.countryCode ||
                    manager.secondaryCountryCode == targetClub.countryCode) &&
                manager.levelCode in bounds
        }

        if (pool.isEmpty()) return null

        if (random.nextInt(100) < 50) {
            pool.sortWith(
                compareByDescending<LegacyReplacementManagerCandidate<T>> { it.sortV }
                    .thenByDescending { it.sortH }
                    .thenBy { it.sortS },
            )
        } else {
            shuffleLikeCollections(pool, random)
        }
        return pool.first().value
    }

    /** Exact source-order fallback of `best.b.u()`: no RNG draw. */
    fun <T> firstUnemployedNonHuman(
        managers: List<LegacyReplacementManagerCandidate<T>>,
    ): T? = managers.firstOrNull { manager ->
        manager.currentClubId == null && !manager.userControlled
    }?.value

    private fun <T> shuffleLikeCollections(
        values: MutableList<T>,
        random: RandomSource,
    ) {
        for (index in values.lastIndex downTo 1) {
            val other = random.nextInt(index + 1)
            if (other != index) {
                val current = values[index]
                values[index] = values[other]
                values[other] = current
            }
        }
    }
}
