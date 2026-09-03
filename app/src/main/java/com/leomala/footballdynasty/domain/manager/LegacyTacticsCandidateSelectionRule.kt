package com.leomala.footballdynasty.domain.manager

/** Runtime fields consumed by the readable legacy `DialogTatics.e(String,best.o)` path. */
data class LegacyTacticsRuntimePlayer<T>(
    val value: T,
    val positionCode: Int,
    val subroleCode: Int,
    val skill: Int,
    val star: Boolean,
)

data class LegacyTacticsCandidateSelectionResult<T>(
    /** Candidate order is captured before the legacy roster sort and therefore preserves input order. */
    val candidates: List<LegacyTacticsRuntimePlayer<T>>,
    /** Mirrors the separate `Collections.sort(team.Z(), components.f3.s)` side effect. */
    val rosterAfterLegacySort: List<LegacyTacticsRuntimePlayer<T>>,
)

/**
 * Exact candidate filtering and roster ordering from `DialogTatics.e(String,best.o)` plus the
 * referenced `components.f3.s` comparator in the official corpus.
 */
object LegacyTacticsCandidateSelectionRule {
    const val cornerTakerActionKey: String = "bEscanteios"
    const val falseNineActionKey: String = "fNove"

    fun <T> select(
        actionKey: String,
        roster: List<LegacyTacticsRuntimePlayer<T>>,
    ): LegacyTacticsCandidateSelectionResult<T> {
        val candidates = when (actionKey) {
            cornerTakerActionKey -> roster.filter { player -> player.positionCode != 0 }
            falseNineActionKey -> roster.filter { player ->
                player.positionCode == 4 ||
                    (player.positionCode == 3 && player.subroleCode == 1)
            }
            else -> roster.toList()
        }

        return LegacyTacticsCandidateSelectionResult(
            candidates = candidates,
            rosterAfterLegacySort = roster.sortedWith(::compareLegacyPlayers),
        )
    }

    private fun <T> compareLegacyPlayers(
        first: LegacyTacticsRuntimePlayer<T>,
        second: LegacyTacticsRuntimePlayer<T>,
    ): Int {
        if (first.positionCode > second.positionCode) return 1
        if (first.positionCode < second.positionCode) return -1
        if (first.subroleCode > second.subroleCode) return 1
        if (first.subroleCode < second.subroleCode) return -1
        if (first.skill > second.skill) return -1
        if (first.skill < second.skill) return 1
        if (first.star && !second.star) return -1
        if (!first.star && second.star) return 1
        return 0
    }
}
