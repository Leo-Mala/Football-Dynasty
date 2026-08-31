package com.leomala.footballdynasty.domain.manager

data class LegacyLineupRuntimePlayer<T>(
    val value: T,
    val positionCode: Int,
    val sideCode: Int,
    val subroleCode: Int,
    val skill: Int,
    val energy: Int,
    val star: Boolean,
)

data class LegacyLineupFormationSlot<T>(val player: LegacyLineupRuntimePlayer<T>?, val slotCode: Int)

data class LegacyLineupPreparedState<T>(
    val formationIndex: Int,
    val clubFormationOption: Int,
    val starters: List<LegacyLineupFormationSlot<T>>,
    val bench: List<LegacyLineupRuntimePlayer<T>>,
    val snapshot: LegacySavedLineupSnapshot<T>,
)

data class LegacySavedLineupSnapshot<T>(
    val formationIndex: Int,
    val players: List<LegacyLineupRuntimePlayer<T>?>,
    val slotCodes: List<Int>,
)

object LegacyLineupFormationTables {
    val slotRequirements: List<IntArray> = listOf(
        intArrayOf(0,-1,-1), intArrayOf(0,-1,-1), intArrayOf(1,1,-1), intArrayOf(2,1,-1),
        intArrayOf(2,-1,-1), intArrayOf(2,0,-1), intArrayOf(2,1,-1), intArrayOf(2,-1,-1),
        intArrayOf(2,0,-1), intArrayOf(1,0,-1), intArrayOf(1,1,1), intArrayOf(3,1,0),
        intArrayOf(3,-1,0), intArrayOf(3,0,0), intArrayOf(3,1,1), intArrayOf(3,-1,1),
        intArrayOf(3,0,1), intArrayOf(1,0,0), intArrayOf(4,1,-1), intArrayOf(4,1,-1),
        intArrayOf(4,-1,-1), intArrayOf(4,0,-1), intArrayOf(4,1,-1), intArrayOf(4,-1,-1),
        intArrayOf(4,0,-1), intArrayOf(4,0,-1),
    )
    val positionFallbackOrder: List<IntArray> = listOf(
        intArrayOf(0,2,1,3,4), intArrayOf(1,3,2,4,0), intArrayOf(2,1,3,4,0),
        intArrayOf(3,1,2,4,0), intArrayOf(4,3,1,2,0),
    )
    val benchPreferenceSlots: IntArray = intArrayOf(1,2,4,4,12,15,15,20,20,23,23)
    val formationSlots: List<IntArray> = listOf(
        intArrayOf(1,22,16,12,14,15,11,2,9,6,8), intArrayOf(1,20,11,13,14,16,2,9,6,4,8),
        intArrayOf(1,22,24,12,14,16,2,9,6,4,8), intArrayOf(1,23,11,13,15,2,9,6,8,10,17),
        intArrayOf(1,22,24,11,13,14,16,2,9,3,5), intArrayOf(1,19,21,11,12,13,15,2,9,6,8),
        intArrayOf(1,22,24,12,14,15,16,2,9,6,8), intArrayOf(1,22,23,24,12,14,16,2,9,6,8),
        intArrayOf(1,19,20,21,11,13,15,2,9,6,8), intArrayOf(1,22,24,11,13,15,4,6,8,10,17),
        intArrayOf(1,18,25,23,11,13,4,6,8,10,17),
    )
    val starterDisplayOrder: IntArray = intArrayOf(1,9,5,4,3,8,7,6,2,17,13,12,11,16,15,14,10,21,20,19,25,24,23,22,18)
}

/** ActivityEscalacao I/J/k/l/Q plus components.y3.e/c. */
object LegacyLineupFormationRuntimeRule {
    fun <T> buildAutomatic(formationIndex: Int, eligibleRoster: List<LegacyLineupRuntimePlayer<T>>, unavailableRoster: List<LegacyLineupRuntimePlayer<T>>): LegacyLineupPreparedState<T> {
        val working = eligibleRoster.toMutableList()
        val starters = LegacyLineupFormationTables.formationSlots[formationIndex].map { slotCode ->
            val player = selectForSlot(working, slotCode)
            if (player != null) removeIdentity(working, player)
            LegacyLineupFormationSlot(player, slotCode)
        }.sortedWith(starterSlotComparator())
        return prepared(formationIndex, formationIndex, starters, buildBench(starters, eligibleRoster, unavailableRoster))
    }

    fun <T> applySaved(snapshot: LegacySavedLineupSnapshot<T>, eligibleRoster: List<LegacyLineupRuntimePlayer<T>>, unavailableRoster: List<LegacyLineupRuntimePlayer<T>>): LegacyLineupPreparedState<T> {
        val clubFormationOption = snapshot.formationIndex.takeIf { it in 0..10 } ?: 0
        val starters = if (snapshot.players.size == 11 && snapshot.slotCodes.size == 11) {
            snapshot.players.indices.map { index ->
                val candidate = snapshot.players[index]
                LegacyLineupFormationSlot(candidate?.takeIf { containsIdentity(eligibleRoster, it) }, snapshot.slotCodes[index])
            }
        } else emptyList()
        val bench = buildBench(starters, eligibleRoster, unavailableRoster)
        if (snapshot.formationIndex !in 0..10) throw IndexOutOfBoundsException("legacy saved formation index: ${snapshot.formationIndex}")
        return prepared(snapshot.formationIndex, clubFormationOption, starters, bench)
    }

    fun <T> selectForSlot(roster: List<LegacyLineupRuntimePlayer<T>>, slotCode: Int): LegacyLineupRuntimePlayer<T>? {
        val requirement = LegacyLineupFormationTables.slotRequirements[slotCode]
        val basePosition = requirement[0]
        var sideCode = requirement[1]
        var subroleCode = requirement[2]
        val sideWasWildcard = sideCode == -1
        val subroleWasWildcard = subroleCode == -1
        if (slotCode >= 18) subroleCode = -1
        for (fallbackIndex in 0..4) {
            var positionCode = LegacyLineupFormationTables.positionFallbackOrder[basePosition][fallbackIndex]
            for (relaxation in 1..4) {
                roster.forEach { player ->
                    if (sideWasWildcard) sideCode = player.sideCode
                    if (subroleWasWildcard) subroleCode = player.subroleCode
                    if (relaxation == 2) sideCode = player.sideCode
                    if (relaxation == 3) { sideCode = player.sideCode; subroleCode = player.subroleCode }
                    if (relaxation == 4) positionCode = player.positionCode
                    if (positionCode == player.positionCode && sideCode == player.sideCode && subroleCode == player.subroleCode) return player
                }
            }
        }
        return null
    }

    private fun <T> buildBench(starters: List<LegacyLineupFormationSlot<T>>, eligibleRoster: List<LegacyLineupRuntimePlayer<T>>, unavailableRoster: List<LegacyLineupRuntimePlayer<T>>): List<LegacyLineupRuntimePlayer<T>> {
        val starterPlayers = starters.mapNotNull { it.player }
        val remaining = eligibleRoster.filterNot { containsIdentity(starterPlayers, it) }.toMutableList()
        val bench = mutableListOf<LegacyLineupRuntimePlayer<T>>()
        LegacyLineupFormationTables.benchPreferenceSlots.forEach { preferredSlot ->
            selectForSlot(remaining, preferredSlot)?.let { chosen -> bench += chosen; removeIdentity(remaining, chosen) }
        }
        bench += remaining
        // l() compares o1 wrappers against player objects; every unavailable player is appended.
        bench += unavailableRoster
        return bench
    }

    private fun <T> prepared(formationIndex: Int, clubFormationOption: Int, starters: List<LegacyLineupFormationSlot<T>>, bench: List<LegacyLineupRuntimePlayer<T>>): LegacyLineupPreparedState<T> {
        val snapshot = LegacySavedLineupSnapshot(formationIndex, starters.map { it.player }, starters.map { it.slotCode })
        return LegacyLineupPreparedState(formationIndex, clubFormationOption, starters, bench, snapshot)
    }

    private fun <T> starterSlotComparator(): Comparator<LegacyLineupFormationSlot<T>> {
        val priority = LegacyLineupFormationTables.starterDisplayOrder.withIndex().associate { it.value to it.index }
        return Comparator { a,b -> (priority[a.slotCode] ?: Int.MAX_VALUE).compareTo(priority[b.slotCode] ?: Int.MAX_VALUE) }
    }
    private fun <T> containsIdentity(list: List<LegacyLineupRuntimePlayer<T>>, target: LegacyLineupRuntimePlayer<T>) = list.any { it === target }
    private fun <T> removeIdentity(list: MutableList<LegacyLineupRuntimePlayer<T>>, target: LegacyLineupRuntimePlayer<T>) { val i=list.indexOfFirst{it===target}; if(i>=0) list.removeAt(i) }
}
