package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionPlayerRatingEntity
import com.leomala.footballdynasty.domain.competition.LegacyCompetitionPlayerRatingAggregateRules

/** One proven legacy `best.k0.a(best.o)` input after a player's match rating is resolved. */
data class CareerCompetitionPlayerRatingMutation(
    val playerId: String,
    val ratingY0: Double,
    val legacyG0: Int,
    val legacyL0: Int,
    val legacyF0: Int,
    val legacyR: Int,
)

/** Durable owner for serialized legacy `best.k0.g` / `components.n1`. */
class CareerCompetitionPlayerRatingStore(
    private val database: FootballDynastyDatabase,
) {
    suspend fun load(
        careerId: String,
        competitionId: String,
    ): List<CareerCompetitionPlayerRatingEntity> =
        database.careerCompetitionDao().playerRatings(careerId, competitionId)

    suspend fun applyForMatch(
        careerId: String,
        matchId: String,
        mutationsInLegacyOrder: List<CareerCompetitionPlayerRatingMutation>,
    ): List<CareerCompetitionPlayerRatingEntity> = database.withTransaction {
        applyForMatchInCurrentTransaction(careerId, matchId, mutationsInLegacyOrder)
    }

    /**
     * Caller-owned transaction helper for the atomic match commit path. The recovered legacy match
     * has zero or one competition owner. Multiple persisted links therefore fail closed instead of
     * choosing an arbitrary competition.
     */
    internal suspend fun applyForMatchInCurrentTransaction(
        careerId: String,
        matchId: String,
        mutationsInLegacyOrder: List<CareerCompetitionPlayerRatingMutation>,
    ): List<CareerCompetitionPlayerRatingEntity> {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        require(matchId.isNotBlank()) { "Match id must not be blank" }
        require(mutationsInLegacyOrder.map { it.playerId }.none { it.isBlank() }) {
            "Competition rating player id must not be blank"
        }

        val dao = database.careerCompetitionDao()
        val links = dao.matchLinksForMatch(careerId, matchId)
        if (links.isEmpty()) return emptyList()
        require(links.size == 1) {
            "Legacy match $careerId/$matchId must resolve zero or one competition, found ${links.size}"
        }
        val competitionId = links.single().competitionId
        val competition = requireNotNull(dao.findCompetition(careerId, competitionId)) {
            "Missing competition $competitionId linked to match $careerId/$matchId"
        }

        val changed = mutableListOf<CareerCompetitionPlayerRatingEntity>()
        mutationsInLegacyOrder.forEach { mutation ->
            val existing = dao.findPlayerRating(careerId, competitionId, mutation.playerId)
            val aggregate = LegacyCompetitionPlayerRatingAggregateRules.apply(
                existing = existing?.toAggregate(),
                input = LegacyCompetitionPlayerRatingAggregateRules.Input(
                    legacyCompetitionType = competition.legacyCompetitionType,
                    ratingY0 = mutation.ratingY0,
                    legacyG0 = mutation.legacyG0,
                    legacyL0 = mutation.legacyL0,
                    legacyF0 = mutation.legacyF0,
                    legacyR = mutation.legacyR,
                ),
            ) ?: return@forEach

            val entity = CareerCompetitionPlayerRatingEntity(
                careerId = careerId,
                competitionId = competitionId,
                playerId = mutation.playerId,
                legacyRatingSum = aggregate.legacyRatingSum,
                legacyRatingCount = aggregate.legacyRatingCount,
                legacyAverageRating = aggregate.legacyAverageRating,
                legacyCategory = aggregate.legacyCategory,
            )
            dao.upsertPlayerRating(entity)
            changed += entity
        }
        return changed
    }

    private fun CareerCompetitionPlayerRatingEntity.toAggregate() =
        LegacyCompetitionPlayerRatingAggregateRules.Aggregate(
            legacyRatingSum = legacyRatingSum,
            legacyRatingCount = legacyRatingCount,
            legacyAverageRating = legacyAverageRating,
            legacyCategory = legacyCategory,
        )
}
