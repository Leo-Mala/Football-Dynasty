package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.competition.LegacyCompetitionSnapshotRules

/**
 * Process-local owner for legacy `konrent.t.k0: ArrayList<components.z2>`.
 *
 * The legacy field is `transient`: it survives consecutive matches while the same competition
 * object remains alive, but it is deliberately lost on save/reopen. This runtime mirrors that
 * boundary and never writes the z2 buffer to Room. Callers stage a candidate view before the Room
 * transaction and commit it only after the transaction succeeds, so rollback cannot advance the
 * transient buffer independently of durable match state.
 */
class CareerLeagueRoundTransientRatingRuntime {
    data class Key(
        val careerId: String,
        val competitionId: String,
        val roundNumber: Int,
    )

    data class CaptureInput(
        val playerId: String,
        val clubIdAtSnapshot: String,
        val ratingY0: Double,
        val legacyG0: Int,
        val randomOrder: Int,
    )

    data class Staged(
        val key: Key,
        val candidatesInLegacyOrder: List<LegacyCompetitionSnapshotRules.RoundCandidate>,
    )

    private val buffers = mutableMapOf<Key, List<LegacyCompetitionSnapshotRules.RoundCandidate>>()

    @Synchronized
    fun stage(
        key: Key,
        capturesInLegacyOrder: List<CaptureInput>,
    ): Staged {
        validateKey(key)
        capturesInLegacyOrder.forEach(::validateCapture)
        val existing = buffers[key].orEmpty()
        val appended = capturesInLegacyOrder.mapIndexed { index, capture ->
            LegacyCompetitionSnapshotRules.RoundCandidate(
                playerId = capture.playerId,
                clubIdAtSnapshot = capture.clubIdAtSnapshot,
                ratingY0 = capture.ratingY0,
                legacyG0 = capture.legacyG0,
                randomOrder = capture.randomOrder,
                stableOrdinal = existing.size + index,
            )
        }
        return Staged(key, existing + appended)
    }

    /**
     * Applies a previously staged view after the durable transaction succeeds. Closing a round
     * mirrors `konrent.t.l0()` clearing the transient source buffer after creating serialized h0.
     */
    @Synchronized
    fun commit(staged: Staged, roundClosed: Boolean) {
        validateKey(staged.key)
        // Entering/committing a newer round makes any stale buffer for the same competition obsolete.
        buffers.keys
            .filter {
                it.careerId == staged.key.careerId &&
                    it.competitionId == staged.key.competitionId &&
                    it != staged.key
            }
            .forEach(buffers::remove)
        if (roundClosed) {
            buffers.remove(staged.key)
        } else {
            buffers[staged.key] = staged.candidatesInLegacyOrder
        }
    }

    @Synchronized
    fun buffered(key: Key): List<LegacyCompetitionSnapshotRules.RoundCandidate> =
        buffers[key].orEmpty().toList()

    @Synchronized
    fun clearCareer(careerId: String) {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        buffers.keys.filter { it.careerId == careerId }.forEach(buffers::remove)
    }

    private fun validateKey(key: Key) {
        require(key.careerId.isNotBlank()) { "Career id must not be blank" }
        require(key.competitionId.isNotBlank()) { "Competition id must not be blank" }
        require(key.roundNumber > 0) { "Legacy competition round must be positive" }
    }

    private fun validateCapture(capture: CaptureInput) {
        require(capture.playerId.isNotBlank()) { "Player id must not be blank" }
        require(capture.clubIdAtSnapshot.isNotBlank()) { "Snapshot club id must not be blank" }
        require(capture.randomOrder in 0..99) { "Legacy z2 random order must be in 0..99" }
    }
}
