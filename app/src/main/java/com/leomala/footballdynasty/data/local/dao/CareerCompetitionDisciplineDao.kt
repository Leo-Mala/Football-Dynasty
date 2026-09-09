package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionDisciplineEntity

@Dao
interface CareerCompetitionDisciplineDao {
    @Upsert
    suspend fun upsert(entity: CareerCompetitionDisciplineEntity)

    @Upsert
    suspend fun upsertAll(entities: List<CareerCompetitionDisciplineEntity>)

    @Query(
        "SELECT * FROM career_player_competition_discipline " +
            "WHERE careerId = :careerId AND playerId = :playerId AND competitionId = :competitionId LIMIT 1"
    )
    suspend fun find(
        careerId: String,
        playerId: String,
        competitionId: String,
    ): CareerCompetitionDisciplineEntity?

    @Query(
        "SELECT * FROM career_player_competition_discipline " +
            "WHERE careerId = :careerId AND competitionId = :competitionId " +
            "ORDER BY playerId ASC"
    )
    suspend fun forCompetition(
        careerId: String,
        competitionId: String,
    ): List<CareerCompetitionDisciplineEntity>
}
