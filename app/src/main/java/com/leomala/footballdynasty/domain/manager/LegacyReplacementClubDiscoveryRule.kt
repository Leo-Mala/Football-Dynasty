package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Club fields read by legacy replacement-club discovery. Names intentionally keep raw semantics. */
class LegacyReplacementSearchClub<T>(
    val identityKey: String,
    val value: T,
    /** Legacy `best.c0.Q0()`. */
    val rawQ0: Boolean,
)

/** Legacy `konrent.t`: its `N0()`/`H` club order is observable. */
class LegacyReplacementSearchCompetition<T>(
    val clubs: List<LegacyReplacementSearchClub<T>>,
)

/**
 * Legacy `best.x` projection used only by `best.b.B(...)`.
 *
 * [rawY] is `x.y()`, [rawU] is `x.u()`, [competitions] is the ordered `x.o` list, and
 * [fallbackClubs] is `x.S0()`. Context instances deliberately use reference identity because
 * `best.b.B` compares `x` objects with JVM `if-eq/if-ne`, not value equality.
 */
class LegacyReplacementSearchContext<T>(
    val rawY: Int,
    val rawU: Int,
    val competitions: List<LegacyReplacementSearchCompetition<T>>,
    val fallbackClubs: List<LegacyReplacementSearchClub<T>> = emptyList(),
)

data class LegacyReplacementSearchManager(
    /** Legacy `best.f0.u()`. */
    val rawU: Int,
    /** Legacy `best.f0.E()`. */
    val rawE: Int,
    /** Legacy `best.f0.K()`. */
    val userControlled: Boolean,
    /** Legacy `best.f0.D()`. */
    val rawD: Int,
    /** Backing value observed through legacy `best.f0.w()`; values above 5 return as 5. */
    val rawW: Int,
    /** Legacy `best.f0.A()?.O()`; null means the `D()` fallback is used. */
    val currentClubDivisionValue: Int?,
    /** Identity of legacy `best.f0.F()`, excluded by `konrent.t.H0` and `best.x.G0`. */
    val excludedClubIdentityKey: String?,
)

/**
 * Java↔SMALI reconstruction of the replacement-club discovery chain
 * `best.b.B -> best.x.H0/G0 -> konrent.t.H0` from the official `com.brasfoot.v2020` corpus.
 *
 * The legacy code creates implicit `Random`/`Collections.shuffle` sources. The modern runtime routes
 * every observable draw through [RandomSource] in the exact call/bound order while making no claim
 * that legacy wall-clock seeding can be reproduced.
 */
object LegacyReplacementClubDiscoveryRule {
    private val primary = arrayOf(
        intArrayOf(0, 0, 1, 2, -1, -1),
        intArrayOf(1, 1, 2, 3, -1, -1),
        intArrayOf(2, 2, 3, -1, -1, -1),
        intArrayOf(2, 3, 3, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1, -1, -1),
    )
    private val primaryAbove700 = arrayOf(
        intArrayOf(0, -1, -1, 2, -1, -1),
        intArrayOf(1, -1, 2, -1, -1, -1),
        intArrayOf(2, -1, -1, -1, -1, -1),
        intArrayOf(2, 3, -1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1, -1, -1),
    )
    private val primaryAbove500 = arrayOf(
        intArrayOf(0, -1, -1, -1, -1, -1),
        intArrayOf(1, -1, -1, -1, -1, -1),
        intArrayOf(2, -1, -1, -1, -1, -1),
        intArrayOf(2, 3, -1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1, -1, -1),
    )
    private val secondary = arrayOf(
        intArrayOf(0, 1, -1, -1, -1, -1),
        intArrayOf(1, 2, -1, -1, -1, -1),
        intArrayOf(2, 8, -1, -1, -1, -1),
        intArrayOf(3, 8, -1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1, -1, -1),
    )
    private val secondaryAbove500 = arrayOf(
        intArrayOf(0, -1, -1, -1, -1, -1),
        intArrayOf(1, -1, -1, -1, -1, -1),
        intArrayOf(8, 8, -1, -1, -1, -1),
        intArrayOf(8, 8, -1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1, -1, -1),
    )

    /** Exact `best.b.B(manager, primaryWindow)` orchestration. */
    fun <T> collectFromWorld(
        contexts: List<LegacyReplacementSearchContext<T>>,
        manager: LegacyReplacementSearchManager,
        primaryWindow: Boolean,
        random: RandomSource,
    ): List<T> {
        val output = mutableListOf<LegacyReplacementSearchClub<T>>()
        val t0 = contexts.firstOrNull { it.rawY == manager.rawU }
        val u0 = contexts.firstOrNull { it.rawU == manager.rawE }

        // SMALI order is U0 first with z3=true, then distinct T0 with z3=false.
        if (u0 != null) {
            collectFromContext(u0, manager, primaryWindow, true, output, random)
        }
        if (t0 != null && t0 !== u0) {
            collectFromContext(t0, manager, primaryWindow, false, output, random)
        }

        if (shouldSearchOtherContexts(manager)) {
            val otherContexts = contexts.filterTo(mutableListOf()) { it !== u0 && it !== t0 }
            if (otherContexts.isNotEmpty()) {
                shuffleLikeCollections(otherContexts, random)
                for (context in otherContexts) {
                    collectFromContext(context, manager, primaryWindow, false, output, random)
                    // Legacy checks after each complete context call and may overshoot seven.
                    if (output.size > 6) break
                }
            }
        }
        return output.map { it.value }
    }

    /** `K && !(D >= 2 && w() <= 3)`, including the `w()` return clamp at five. */
    fun shouldSearchOtherContexts(manager: LegacyReplacementSearchManager): Boolean {
        val observedW = if (manager.rawW > 5) 5 else manager.rawW
        return manager.userControlled && !(manager.rawD >= 2 && observedW <= 3)
    }

    /** Exact `best.x.H0(...)`. [primaryContext] is legacy fourth argument `z3`. */
    fun <T> collectFromContext(
        context: LegacyReplacementSearchContext<T>,
        manager: LegacyReplacementSearchManager,
        primaryWindow: Boolean,
        primaryContext: Boolean,
        output: MutableList<LegacyReplacementSearchClub<T>>,
        random: RandomSource,
    ) {
        var divisionIndex = manager.currentClubDivisionValue?.minus(1) ?: manager.rawD
        if (divisionIndex < 0) divisionIndex = context.competitions.size - 1
        if (divisionIndex > context.competitions.size - 1) {
            divisionIndex = context.competitions.size - 1
        }
        if (divisionIndex < 0) return

        val draw = random.nextInt(1000)
        var indexes = when {
            draw > 700 -> primaryAbove700[divisionIndex]
            draw > 500 -> primaryAbove500[divisionIndex]
            else -> primary[divisionIndex]
        }.copyOf()
        if (!primaryContext) {
            indexes = (if (draw > 500) secondaryAbove500 else secondary)[divisionIndex].copyOf()
        }

        for (slot in indexes.indices) {
            if (indexes[slot] == 8) indexes[slot] = context.competitions.size - 1
            val competitionIndex = indexes[slot]
            if (competitionIndex < 0 || competitionIndex >= context.competitions.size) continue
            val competition = context.competitions[competitionIndex]
            if (competition.clubs.isNotEmpty()) {
                collectFromCompetition(competition, manager, primaryWindow, output, random)
            } else if (!manager.userControlled) {
                collectFromFallback(context, manager, output, random)
            }
        }
    }

    /** Exact `konrent.t.H0(...)`: candidate window, filter, shuffle, first unseen only. */
    fun <T> collectFromCompetition(
        competition: LegacyReplacementSearchCompetition<T>,
        manager: LegacyReplacementSearchManager,
        primaryWindow: Boolean,
        output: MutableList<LegacyReplacementSearchClub<T>>,
        random: RandomSource,
    ) {
        val candidates = mutableListOf<LegacyReplacementSearchClub<T>>()
        var index: Int
        var endExclusive: Int
        if (primaryWindow) {
            index = 2
            endExclusive = 6
        } else {
            index = competition.clubs.size - 6
            endExclusive = competition.clubs.size
        }
        if (endExclusive > competition.clubs.size) endExclusive = competition.clubs.size

        // Do not clamp a negative secondary-window start: legacy ArrayList.get would fail likewise.
        while (index < endExclusive) {
            val club = competition.clubs[index]
            if (!club.rawQ0 && club.identityKey != manager.excludedClubIdentityKey) {
                candidates += club
            }
            index++
        }

        shuffleLikeCollections(candidates, random)
        for (club in candidates) {
            if (output.none { it.identityKey == club.identityKey }) {
                output += club
                return
            }
        }
    }

    /** Exact `best.x.G0(...)`: unlike competition H0, a duplicated shuffled first item stops search. */
    fun <T> collectFromFallback(
        context: LegacyReplacementSearchContext<T>,
        manager: LegacyReplacementSearchManager,
        output: MutableList<LegacyReplacementSearchClub<T>>,
        random: RandomSource,
    ) {
        val candidates = context.fallbackClubs.filterTo(mutableListOf()) { club ->
            !club.rawQ0 && club.identityKey != manager.excludedClubIdentityKey
        }
        shuffleLikeCollections(candidates, random)
        val first = candidates.firstOrNull() ?: return
        if (output.none { it.identityKey == first.identityKey }) output += first
    }

    private fun <T> shuffleLikeCollections(values: MutableList<T>, random: RandomSource) {
        for (index in values.lastIndex downTo 1) {
            val other = random.nextInt(index + 1)
            if (other != index) {
                val tmp = values[index]
                values[index] = values[other]
                values[other] = tmp
            }
        }
    }
}
