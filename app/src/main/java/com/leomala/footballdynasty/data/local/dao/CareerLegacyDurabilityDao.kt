package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionSnapshotEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionSnapshotMemberEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerMatchRatingHistoryEntity

@Dao
interface CareerLegacyDurabilityDao {
    @Upsert
    suspend fun upsertSnapshot(entity: CareerCompetitionSnapshotEntity)

    @Upsert
    suspend fun upsertSnapshotMembers(entities: List<CareerCompetitionSnapshotMemberEntity>)

    @Query(
        "SELECT * FROM career_competition_snapshots " +
            "WHERE careerId = :careerId AND competitionId = :competitionId AND snapshotKind = :snapshotKind " +
            "ORDER BY snapshotOrdinal ASC"
    )
    suspend fun snapshots(
        careerId: String,
        competitionId: String,
        snapshotKind: String,
    ): List<CareerCompetitionSnapshotEntity>

    @Query(
        "SELECT * FROM career_competition_snapshot_members " +
            "WHERE careerId = :careerId AND competitionId = :competitionId " +
            "AND snapshotKind = :snapshotKind AND snapshotOrdinal = :snapshotOrdinal " +
            "ORDER BY memberOrdinal ASC"
    )
    suspend fun snapshotMembers(
        careerId: String,
        competitionId: String,
        snapshotKind: String,
        snapshotOrdinal: Int,
    ): List<CareerCompetitionSnapshotMemberEntity>

    @Upsert
    suspend fun upsertPlayerMatchRatingHistory(entity: CareerPlayerMatchRatingHistoryEntity)

    @Query(
        "SELECT * FROM career_player_match_rating_history " +
            "WHERE careerId = :careerId AND playerId = :playerId " +
            "ORDER BY legacySeasonIndex ASC, legacyMatchIndex ASC"
    )
    suspend fun playerMatchRatingHistory(
        careerId: String,
        playerId: String,
    ): List<CareerPlayerMatchRatingHistoryEntity>

    @Query("DELETE FROM career_player_match_rating_history WHERE careerId = :careerId")
    suspend fun clearPlayerMatchRatingHistory(careerId: String)
}
