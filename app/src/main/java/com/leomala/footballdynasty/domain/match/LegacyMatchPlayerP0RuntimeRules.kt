package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.domain.manager.LegacyLineupFormationTables

/**
 * Connects the committed lineup slot (`best.o.g0`) to the already-characterized player `P0()`
 * predicate without introducing a second slot table.
 *
 * Starter lineup codes 1..25 are written by [com.leomala.footballdynasty.domain.manager.LegacyLineupCommitRule]
 * from the exact formation slot code and later hydrated as [LegacyMatchTransientRuntime.Player.legacyG0].
 * Bench/unset codes do not require a slot-table lookup because [LegacyMatchPlayerP0Rules] resolves
 * those branches before consuming the base-position owner.
 */
object LegacyMatchPlayerP0RuntimeRules {
    fun <T> resolve(player: LegacyMatchTransientRuntime.Player<T>): Boolean {
        val legacyS = player.legacyG0
        val slotBasePosition = if (legacyS in 1..25) {
            LegacyLineupFormationTables.slotRequirements[legacyS][0]
        } else {
            null
        }
        return LegacyMatchPlayerP0Rules.resolve(
            legacyS = legacyS,
            legacyL0 = player.legacyL0,
            slotBasePosition = slotBasePosition,
        )
    }
}
