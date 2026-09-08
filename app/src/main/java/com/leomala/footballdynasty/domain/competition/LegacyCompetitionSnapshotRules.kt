package com.leomala.footballdynasty.domain.competition

/**
 * Exact pure selection rules for the two serialized legacy `best.h0` owners.
 *
 * `best.k0.c(index)` snapshots the season aggregate (`k0.g`) after `k0.U()` sorting, while
 * `konrent.t.l0()` snapshots the transient per-round `components.z2` buffer. Persistence and
 * lifecycle timing remain owned by the Room/application layer.
 */
object LegacyCompetitionSnapshotRules {
    private val ANNUAL_SELECTORS = intArrayOf(0, 1, 2, 2, 5, 6, 6, 3, 3, 4, 4)
    private val ROUND_SLOT_MIN = intArrayOf(1, 9, 3, 3, 2, 11, 11, 14, 14, 18, 18)
    private val ROUND_SLOT_MAX = intArrayOf(1, 9, 8, 8, 2, 13, 13, 16, 16, 25, 25)

    data class Member(
        val playerId: String,
        /** Legacy `best.k0.c()` appends player.u0() without a null check. */
        val clubIdAtSnapshot: String?,
    )

    data class Snapshot(
        /** Raw `best.h0.a`. */
        val legacyA: Int,
        /** Raw `best.h0.b`. */
        val legacyB: Int,
        val members: List<Member>,
        /** `best.k0.i` entry aligned with an annual `k0.h` snapshot; null for `konrent.t.j0`. */
        val topPlayerId: String?,
        /** Exact `best.k0.c`: `k0.b == 1 && index == 0` marks the selected top player as star. */
        val markTopPlayerStar: Boolean,
    )

    data class AnnualCandidate(
        val playerId: String,
        val clubIdAtSnapshot: String?,
        val legacyAverage: Double,
        val legacyCount: Double,
        val legacySelector: Int,
        /** Original `k0.g` insertion order, needed because Java's object sort is stable on ties. */
        val stableOrdinal: Int,
    )

    data class RoundCandidate(
        val playerId: String,
        val clubIdAtSnapshot: String,
        val ratingY0: Double,
        val legacyG0: Int,
        val randomOrder: Int,
        /** Original transient `k0` insertion order for a full comparator tie. */
        val stableOrdinal: Int,
    )

    fun annual(
        candidates: List<AnnualCandidate>,
        threshold: Int,
        legacyYear: Int,
        legacyCompetitionType: Int,
        competitionIndex: Int,
    ): Snapshot {
        require(threshold >= 0) { "Legacy threshold must not be negative" }
        require(competitionIndex >= 0) { "Competition index must not be negative" }
        validateAnnual(candidates)

        val sorted = candidates.sortedWith(
            compareByDescending<AnnualCandidate> { it.legacyAverage }
                .thenByDescending { it.legacyCount }
                .thenBy { it.stableOrdinal }
        )
        val selected = mutableListOf<AnnualCandidate>()
        ANNUAL_SELECTORS.forEach { selector ->
            sorted.firstOrNull { candidate ->
                candidate.legacySelector == selector &&
                    candidate.legacyCount >= threshold.toDouble() &&
                    selected.none { it.playerId == candidate.playerId }
            }?.let(selected::add)
        }
        val top = sorted.firstOrNull { it.legacyCount >= threshold.toDouble() }
        return Snapshot(
            legacyA = legacyYear,
            legacyB = 0,
            members = selected.map { Member(it.playerId, it.clubIdAtSnapshot) },
            topPlayerId = top?.playerId,
            markTopPlayerStar = top != null && legacyCompetitionType == 1 && competitionIndex == 0,
        )
    }

    fun round(
        candidates: List<RoundCandidate>,
        legacyYear: Int,
        existingSnapshotCount: Int,
    ): Snapshot {
        require(existingSnapshotCount >= 0) { "Existing snapshot count must not be negative" }
        validateRound(candidates)

        val sorted = candidates.sortedWith(
            compareByDescending<RoundCandidate> { it.ratingY0 }
                .thenByDescending { it.randomOrder }
                .thenBy { it.stableOrdinal }
        )
        val selected = mutableListOf<RoundCandidate>()
        ROUND_SLOT_MIN.indices.forEach { slot ->
            sorted.firstOrNull { candidate ->
                candidate.legacyG0 in ROUND_SLOT_MIN[slot]..ROUND_SLOT_MAX[slot] &&
                    selected.none { it.playerId == candidate.playerId }
            }?.let(selected::add)
        }
        return Snapshot(
            legacyA = legacyYear,
            legacyB = existingSnapshotCount - 1,
            members = selected.map { Member(it.playerId, it.clubIdAtSnapshot) },
            topPlayerId = null,
            markTopPlayerStar = false,
        )
    }

    private fun validateAnnual(candidates: List<AnnualCandidate>) {
        require(candidates.map { it.playerId }.distinct().size == candidates.size) {
            "Annual legacy aggregate candidates must have unique player identities"
        }
        require(candidates.map { it.stableOrdinal }.distinct().size == candidates.size) {
            "Annual legacy aggregate stable ordinals must be unique"
        }
        candidates.forEach { candidate ->
            require(candidate.playerId.isNotBlank()) { "Player id must not be blank" }
            require(candidate.legacyCount >= 0.0) { "Legacy count must not be negative" }
        }
    }

    private fun validateRound(candidates: List<RoundCandidate>) {
        require(candidates.map { it.playerId }.distinct().size == candidates.size) {
            "Round legacy rating candidates must have unique player identities"
        }
        require(candidates.map { it.stableOrdinal }.distinct().size == candidates.size) {
            "Round legacy rating stable ordinals must be unique"
        }
        candidates.forEach { candidate ->
            require(candidate.playerId.isNotBlank()) { "Player id must not be blank" }
            require(candidate.clubIdAtSnapshot.isNotBlank()) { "Snapshot club id must not be blank" }
            require(candidate.randomOrder in 0..99) { "Legacy z2 random order must be in 0..99" }
        }
    }
}
