package com.leomala.footballdynasty.domain.manager

data class LegacyLineupStartPlan(
    val validStarterCount: Int,
    val showProgressAndLockWindow: Boolean,
    val runCommitOnBackgroundThread: Boolean,
    val showSelectPlayersToast: Boolean,
    val legacyFormationResetSequence: List<Int>,
    val rebuildAutomaticAndRetry: Boolean,
)

/** Exact decision path from ActivityEscalacao.x(). */
object LegacyLineupStartRule {
    fun <T> plan(starterSlots: List<LegacyLineupCommitSlot<T>>, eligibleRoster: List<T>, globalD1: Boolean): LegacyLineupStartPlan {
        val validCount = starterSlots.count { slot -> slot.player != null && eligibleRoster.any { it === slot.player } && slot.slotCode > 0 && slot.slotCode < 26 }
        if (validCount >= 11) return LegacyLineupStartPlan(validCount, true, true, false, emptyList(), false)
        return LegacyLineupStartPlan(validCount, false, false, true, if (globalD1) listOf(4,0) else emptyList(), globalD1)
    }
}
