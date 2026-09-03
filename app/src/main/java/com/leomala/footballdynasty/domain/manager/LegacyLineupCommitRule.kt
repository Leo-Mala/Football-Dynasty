package com.leomala.footballdynasty.domain.manager

/** One legacy starter slot as consumed by ActivityEscalacao.y()/Java-facing B(). */
data class LegacyLineupCommitSlot<T>(val player: T?, val slotCode: Int)

data class LegacyLineupPlayerWrite<T>(
    val player: T,
    val lineupCode: Int,
    /** Starters receive s1(TRUE); bench players receive no s1 write at all. */
    val starterFlagWrite: Boolean?,
)

data class LegacyLineupMatchLists<T>(
    val sideIndex: Int,
    /** q outside 0/1 leaves the existing match-side lists untouched rather than clearing them. */
    val replaceSideLists: Boolean,
    val startersPrimary: List<T>,
    val startersMirror: List<T>,
    val bench: List<T>,
)

data class LegacyLineupFinalizePlan(
    val setClubFlagE1True: Boolean,
    val finishMainTeamActivity: Boolean,
    val clearMainTeamStaticActivity: Boolean,
    val setMainTeamGTrue: Boolean,
    val setMainTeamHFalse: Boolean,
    val invokeBestNI: Boolean,
    val finishLineupActivity: Boolean,
)

data class LegacyLineupCommitResult<T>(
    val clubStarters: List<T>,
    val clubBench: List<T>,
    val playerWrites: List<LegacyLineupPlayerWrite<T>>,
    val matchLists: LegacyLineupMatchLists<T>,
    val finalizePlan: LegacyLineupFinalizePlan,
)

/** Exact pure-state projection of ActivityEscalacao.y()/B() from the official corpus. */
object LegacyLineupCommitRule {
    fun <T> commit(starterSlots: List<LegacyLineupCommitSlot<T>>, benchPlayers: List<T?>, eligibleRoster: List<T>, matchSideIndex: Int, mainTeamActivityPresent: Boolean): LegacyLineupCommitResult<T> {
        val clubStarters = mutableListOf<T>(); val clubBench = mutableListOf<T>(); val writes = mutableListOf<LegacyLineupPlayerWrite<T>>()
        starterSlots.forEach { slot ->
            val player = slot.player
            if (player != null && slot.slotCode > 0 && slot.slotCode < 26) {
                writes += LegacyLineupPlayerWrite(player, slot.slotCode, true)
                clubStarters += player
            }
        }
        var acceptedBenchCount = 0
        benchPlayers.forEach { player ->
            if (player != null && eligibleRoster.any { it === player }) {
                acceptedBenchCount += 1
                writes += LegacyLineupPlayerWrite(player, acceptedBenchCount + 25, null)
                clubBench += player
            }
        }
        val replaceSideLists = matchSideIndex == 0 || matchSideIndex == 1
        val propagatedStarters = if (replaceSideLists) clubStarters.toList() else emptyList()
        val propagatedBench = if (replaceSideLists) clubBench.toList() else emptyList()
        return LegacyLineupCommitResult(
            clubStarters, clubBench, writes,
            LegacyLineupMatchLists(matchSideIndex, replaceSideLists, propagatedStarters, propagatedStarters.toList(), propagatedBench),
            LegacyLineupFinalizePlan(true, mainTeamActivityPresent, true, true, true, true, true),
        )
    }
}
