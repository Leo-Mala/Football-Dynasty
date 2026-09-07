package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionSnapshotEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionSnapshotMemberEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerMatchRatingHistoryEntity
import com.leomala.footballdynasty.domain.competition.LegacyCompetitionSnapshotRules

/** One already-resolved legacy `components.s2` write for a player in the current match. */
data class CareerPlayerMatchRatingHistoryMutation(
    val playerId: String,
    val ratingY0: Double,
)

/** Raw serialized `best.s.N0()/A0()` state. Null mutation means this match has no proven write. */
data class CareerMatchTieBreakMutation(
    val legacyTieBreakActive: Boolean,
    val legacyTieBreakWinnerClubId: String?,
)

/** Durable V17 owner for non-reconstructible serialized legacy snapshots/history/tie state. */
class CareerLegacyDurabilityStore(
    private val database: FootballDynastyDatabase,
) {
    suspend fun persistMatchEvidence(
        careerId: String,
        matchId: String,
        playerRatingsInLegacyOrder: List<CareerPlayerMatchRatingHistoryMutation>,
        tieBreakMutation: CareerMatchTieBreakMutation? = null,
    ) = database.withTransaction {
        persistMatchEvidenceInCurrentTransaction(
            careerId = careerId,
            matchId = matchId,
            playerRatingsInLegacyOrder = playerRatingsInLegacyOrder,
            tieBreakMutation = tieBreakMutation,
        )
    }

    internal suspend fun persistMatchEvidenceInCurrentTransaction(
        careerId: String,
        matchId: String,
        playerRatingsInLegacyOrder: List<CareerPlayerMatchRatingHistoryMutation>,
        tieBreakMutation: CareerMatchTieBreakMutation? = null,
    ) {
        require(careerId.isNotBlank())
        require(matchId.isNotBlank())
        require(playerRatingsInLegacyOrder.map { it.playerId }.distinct().size == playerRatingsInLegacyOrder.size) {
            "Legacy components.s2 history may contain at most one row per player/match"
        }

        val scheduledDao = database.careerScheduledMatchDao()
        val scheduled = requireNotNull(scheduledDao.findById(careerId, matchId)) {
            "Missing scheduled match $careerId/$matchId for V17 durability"
        }
        val legacyDayMatchIndex = if (playerRatingsInLegacyOrder.isEmpty()) {
            scheduled.legacyDayMatchOrdinal
        } else {
            requireNotNull(scheduled.legacyDayMatchOrdinal) {
                "Scheduled match $careerId/$matchId has no proven best.a.A() ordinal"
            }
        }
        val durabilityDao = database.careerLegacyDurabilityDao()
        playerRatingsInLegacyOrder.forEach { mutation ->
            require(mutation.playerId.isNotBlank())
            requireNotNull(database.careerPlayerRuntimeDao().findRuntime(careerId, mutation.playerId)) {
                "Missing player runtime ${mutation.playerId} for components.s2 history"
            }
            durabilityDao.upsertPlayerMatchRatingHistory(
                CareerPlayerMatchRatingHistoryEntity(
                    careerId = careerId,
                    playerId = mutation.playerId,
                    legacyDayIndexB = scheduled.dayIndex,
                    legacyDayMatchIndexC = requireNotNull(legacyDayMatchIndex),
                    legacyRating = mutation.ratingY0,
                )
            )
        }

        tieBreakMutation?.let { tie ->
            val winner = tie.legacyTieBreakWinnerClubId
            require(winner == null || winner == scheduled.homeClubId || winner == scheduled.awayClubId) {
                "Legacy explicit tie winner must be one of the scheduled match clubs"
            }
            scheduledDao.upsert(
                scheduled.copy(
                    legacyTieBreakActive = tie.legacyTieBreakActive,
                    legacyTieBreakWinnerClubId = winner,
                )
            )
        }
    }

    suspend fun persistCompetitionSnapshot(
        careerId: String,
        competitionId: String,
        snapshotKind: String,
        snapshot: LegacyCompetitionSnapshotRules.Snapshot,
    ) = database.withTransaction {
        persistCompetitionSnapshotInCurrentTransaction(careerId, competitionId, snapshotKind, snapshot)
    }

    internal suspend fun persistCompetitionSnapshotInCurrentTransaction(
        careerId: String,
        competitionId: String,
        snapshotKind: String,
        snapshot: LegacyCompetitionSnapshotRules.Snapshot,
    ) {
        require(snapshotKind == SNAPSHOT_ANNUAL || snapshotKind == SNAPSHOT_ROUND) {
            "Unsupported legacy snapshot kind $snapshotKind"
        }
        requireNotNull(database.careerCompetitionDao().findCompetition(careerId, competitionId)) {
            "Missing competition $careerId/$competitionId for legacy snapshot"
        }
        val dao = database.careerLegacyDurabilityDao()
        val ordinal = dao.snapshots(careerId, competitionId, snapshotKind).size
        dao.upsertSnapshot(
            CareerCompetitionSnapshotEntity(
                careerId = careerId,
                competitionId = competitionId,
                snapshotKind = snapshotKind,
                snapshotOrdinal = ordinal,
                legacyA = snapshot.legacyA,
                legacyB = snapshot.legacyB,
                topPlayerId = snapshot.topPlayerId,
            )
        )
        dao.upsertSnapshotMembers(
            snapshot.members.mapIndexed { memberOrdinal, member ->
                CareerCompetitionSnapshotMemberEntity(
                    careerId = careerId,
                    competitionId = competitionId,
                    snapshotKind = snapshotKind,
                    snapshotOrdinal = ordinal,
                    memberOrdinal = memberOrdinal,
                    playerId = member.playerId,
                    clubIdAtSnapshot = member.clubIdAtSnapshot,
                )
            }
        )
    }

    suspend fun clearAnnualPlayerRatingHistory(careerId: String) = database.withTransaction {
        database.careerLegacyDurabilityDao().clearPlayerMatchRatingHistory(careerId)
    }

    companion object {
        const val SNAPSHOT_ANNUAL = "ANNUAL"
        const val SNAPSHOT_ROUND = "ROUND"
    }
}
