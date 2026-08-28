package com.leomala.footballdynasty.domain.match

/**
 * Exact mutation parity for legacy `best.o.u()` and the active-player traversal in `best.s.s(...)`.
 *
 * The official Java + SMALI corpus proves this refresh contains no RNG. `best.s.k(...)` invokes it
 * every seventh minute before the event gates. During period 1, active players whose legacy `g0`
 * equals 1 are skipped; period 2 refreshes every active player. Bench players are never traversed.
 */
object LegacyMatchEnergyRules {
    fun decrementForAge(age: Int): Int = when {
        age <= 20 -> 1
        age <= 25 -> 2
        age <= 32 -> 3
        age <= 36 -> 4
        else -> 5
    }

    /**
     * Mirrors `best.o.l(int)`: subtract first, then replace only a negative result with 1.
     * An exact zero is deliberately preserved.
     */
    fun updatedEnergy(age: Int, energy: Int): Int {
        val updated = energy - decrementForAge(age)
        return if (updated < 0) 1 else updated
    }

    fun <TClub, TPlayer> refreshActivePlayers(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>,
        legacyPeriod: Int,
    ) {
        require(legacyPeriod == 1 || legacyPeriod == 2) {
            "Legacy match period must be 1 or 2: $legacyPeriod"
        }

        fun refresh(club: LegacyMatchTransientRuntime.Club<TClub, TPlayer>) {
            club.active.forEach { player ->
                if (player.legacyG0 != 1 || legacyPeriod == 2) {
                    player.energy = updatedEnergy(player.age, player.energy)
                }
            }
        }

        refresh(state.home)
        refresh(state.away)
    }
}
