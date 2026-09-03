package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the raw legacy manager field `best.f0.H`.
 *
 * This deliberately keeps the field name neutral. The official corpus proves that ticket attendance
 * reads `club.y0()?.o()` (which returns H), while manager lifecycle code mutates the same field. No
 * broader user-facing semantic name is assigned here.
 */
object LegacyCoachRawHRule {
    const val INITIAL_VALUE: Int = 80
    const val MAIN_TEAM_FLOOR: Int = 30

    private val matchUpdateCompetitionTypes = setOf(1, 2, 3, 4, 5, 6, 8)

    /** `best.f0` constructor and `best.f0.e(c0)` both materialize/reset H to 80. */
    fun initialValue(): Int = INITIAL_VALUE

    fun afterEmployment(): Int = INITIAL_VALUE

    /**
     * H-only projection of `best.f0.i(best.s)`, reached from `best.s.f()` only for the exact
     * competition-type set above. Every delta goes through legacy `h(int)`, so clamping happens
     * after each individual write rather than once after a summed delta.
     */
    fun afterMatch(
        rawH: Int,
        rawCompetitionType: Int?,
        managerGoals: Int,
        opponentGoals: Int,
        managerIsAway: Boolean,
    ): Int {
        if (rawCompetitionType !in matchUpdateCompetitionTypes) return rawH

        val largeMargin = if (managerGoals >= opponentGoals) {
            managerGoals - opponentGoals >= 3
        } else {
            opponentGoals - managerGoals >= 3
        }

        return when {
            managerGoals == opponentGoals -> addClamped(rawH, if (managerIsAway) 2 else 0)
            managerGoals > opponentGoals -> {
                var value = addClamped(rawH, if (managerIsAway) 5 else 3)
                if (largeMargin) {
                    value = addClamped(value, if (managerIsAway) 7 else 3)
                }
                value
            }
            else -> {
                var value = addClamped(rawH, if (managerIsAway) -3 else -5)
                if (largeMargin) {
                    value = addClamped(value, if (managerIsAway) -2 else -5)
                }
                value
            }
        }
    }

    /** `best.b.s()` calls `club.y0().h(50)` for every club that has a manager. */
    fun afterAnnualRecovery(rawH: Int): Int = addClamped(rawH, 50)

    /**
     * `ActivityMainTeam.F()` writes `N(30)` when its legacy `h` flag is true and current H < 30.
     * The caller owns that already-characterized UI/runtime flag; this rule preserves only the state
     * mutation and does not invent when the screen should be opened.
     */
    fun afterMainTeamRefresh(
        rawH: Int,
        legacyFloorEnabled: Boolean,
    ): Int = if (legacyFloorEnabled && rawH < MAIN_TEAM_FLOOR) MAIN_TEAM_FLOOR else rawH

    private fun addClamped(
        rawH: Int,
        delta: Int,
    ): Int {
        val updated = rawH + delta
        return when {
            updated > 100 -> 100
            updated < 0 -> 0
            else -> updated
        }
    }
}
