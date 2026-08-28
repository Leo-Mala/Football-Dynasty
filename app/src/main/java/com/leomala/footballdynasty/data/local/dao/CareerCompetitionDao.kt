package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionMatchEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionStandingEntity

@Dao
interface CareerCompetitionDao {
    @Upsert
    suspend fun upsertCompetition(entity: CareerCompetitionEntity)

    @Upsert
    suspend fun upsertStandings(entities: List<CareerCompetitionStandingEntity>)

    @Upsert
    suspend fun upsertMatches(entities: List<CareerCompetitionMatchEntity>)

    @Query(
        "SELECT * FROM career_competitions " +
            "WHERE careerId = :careerId AND competitionId = :competitionId LIMIT 1"
    )
    suspend fun findCompetition(careerId: String, competitionId: String): CareerCompetitionEntity?

    @Query(
        "SELECT * FROM career_competition_standings " +
            "WHERE careerId = :careerId AND competitionId = :competitionId " +
            "ORDER BY stableOrdinal ASC"
    )
    suspend fun standings(careerId: String, competitionId: String): List<CareerCompetitionStandingEntity>

    @Query(
        "SELECT * FROM career_competition_matches " +
            "WHERE careerId = :careerId AND competitionId = :competitionId " +
            "ORDER BY roundNumber ASC, fixtureOrdinal ASC"
    )
    suspend fun matches(careerId: String, competitionId: String): List<CareerCompetitionMatchEntity>

    @Query(
        "SELECT * FROM career_competition_matches " +
            "WHERE careerId = :careerId AND competitionId = :competitionId AND roundNumber = :roundNumber " +
            "ORDER BY fixtureOrdinal ASC"
    )
    suspend fun matchesForRound(
        careerId: String,
        competitionId: String,
        roundNumber: Int,
    ): List<CareerCompetitionMatchEntity>

    @Query(
        "SELECT * FROM career_competition_matches " +
            "WHERE careerId = :careerId AND matchId = :matchId " +
            "ORDER BY competitionId ASC"
    )
    suspend fun matchLinksForMatch(careerId: String, matchId: String): List<CareerCompetitionMatchEntity>
}
