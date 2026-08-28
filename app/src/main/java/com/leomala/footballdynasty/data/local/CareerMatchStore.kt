package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerPlayerClubSeasonStatEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.domain.career.CareerIntegrityValidator
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeResult
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.model.Match
import com.leomala.footballdynasty.foundation.error.CareerIntegrityException

data class CareerMatchPlayerRuntimeUpdate(
    val playerId: String,
    val energy: Int,
    val overall: Int,
    val injuryUntilEpochDay: Long,
)

/**
 * Atomic Room boundary for scheduled-match + career RNG/calendar persistence.
 *
 * Proven player effects and a linked league round are committed in the same transaction. The
 * competition table advances only when the just-saved result makes every fixture in its current
 * round resolved, preserving the proven legacy `konrent.t.d0()` result -> table -> U ordering.
 */
class CareerMatchStore(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    suspend fun initializeSchedule(
        state: CareerState,
        schedule: List<ScheduledCareerMatch>,
    ) {
        CareerIntegrityValidator.validate(state)
        validateSchedule(state, schedule)
        database.withTransaction {
            requireCareerOwner(state.id)
            val dao = database.careerScheduledMatchDao()
            require(dao.findAll(state.id).isEmpty()) {
                "Career ${state.id} already has a persisted match schedule"
            }
            database.careerCoreStateDao().upsert(
                CareerCoreStateRoomAdapter.entity(state, clockMillis())
            )
            dao.upsertAll(schedule.map { it.toEntity(state.id) })
        }
    }

    suspend fun loadSchedule(careerId: String): List<ScheduledCareerMatch> =
        database.careerScheduledMatchDao().findAll(careerId).map { it.toScheduledMatch() }

    suspend fun findResult(careerId: String, matchId: String): Match? {
        val entity = database.careerScheduledMatchDao().findById(careerId, matchId) ?: return null
        if (!entity.processed || entity.homeGoals == null || entity.awayGoals == null) return null
        return entity.toMatch()
    }

    /** Commits post-match career state, score, proven player effects and linked round progression. */
    suspend fun commitMatch(
        result: CareerMatchRuntimeResult,
        playerRuntimeUpdates: List<CareerMatchPlayerRuntimeUpdate> = emptyList(),
        playerClubSeasonStatUpdates: List<CareerMatchPlayerClubSeasonStatUpdate> = emptyList(),
    ) {
        CareerIntegrityValidator.validate(result.state)
        validateSchedule(result.state, result.schedule)
        validateResolvedMatch(result)
        validatePlayerRuntimeUpdates(playerRuntimeUpdates)
        validatePlayerClubSeasonStatUpdates(playerClubSeasonStatUpdates)

        database.withTransaction {
            requireCareerOwner(result.state.id)
            val dao = database.careerScheduledMatchDao()
            val persisted = dao.findAll(result.state.id)
            require(persisted.size == result.schedule.size) {
                "Persisted schedule size diverged for career ${result.state.id}"
            }
            val persistedById = persisted.associateBy { it.matchId }
            result.schedule.forEach { scheduled ->
                val current = requireNotNull(persistedById[scheduled.matchId]) {
                    "Scheduled match ${scheduled.matchId} was not persisted"
                }
                requireImmutableIdentity(current, scheduled)
                require(!current.processed || scheduled.processed) {
                    "Scheduled match ${scheduled.matchId} cannot revert to unprocessed"
                }
            }

            val target = result.schedule.single { it.matchId == result.match.id }
            require(target.processed) { "Resolved match ${target.matchId} must be marked processed" }
            val current = requireNotNull(persistedById[target.matchId])
            require(!current.processed) { "Resolved match ${target.matchId} was already committed" }

            database.careerCoreStateDao().upsert(
                CareerCoreStateRoomAdapter.entity(result.state, clockMillis())
            )
            dao.upsert(
                current.copy(
                    processed = true,
                    homeGoals = result.match.homeGoals,
                    awayGoals = result.match.awayGoals,
                )
            )
            persistPlayerRuntimeUpdates(result, playerRuntimeUpdates)
            persistPlayerClubSeasonStatUpdates(result, playerClubSeasonStatUpdates)
            advanceLinkedCompetitionRound(result.state.id, result.match.id)
        }
    }

    private suspend fun advanceLinkedCompetitionRound(careerId: String, matchId: String) {
        val competitionDao = database.careerCompetitionDao()
        val links = competitionDao.matchLinksForMatch(careerId, matchId)
        require(links.size <= 1) { "Scheduled match $matchId belongs to multiple competitions" }
        val link = links.singleOrNull() ?: return
        val competition = requireNotNull(
            competitionDao.findCompetition(careerId, link.competitionId)
        ) { "Missing linked competition ${link.competitionId}" }
        require(link.roundNumber == competition.currentRoundNumber) {
            "Resolved match $matchId does not belong to current competition round"
        }
        CareerCompetitionStore(database).advanceCurrentRoundIfResolvedInCurrentTransaction(
            careerId = careerId,
            competitionId = link.competitionId,
        )
    }

    private suspend fun persistPlayerRuntimeUpdates(
        result: CareerMatchRuntimeResult,
        updates: List<CareerMatchPlayerRuntimeUpdate>,
    ) {
        val playerDao = database.careerPlayerRuntimeDao()
        updates.forEach { update ->
            requireMatchPlayer(result, update.playerId)
            val runtime = requireNotNull(playerDao.findRuntime(result.state.id, update.playerId)) {
                "Missing career player runtime for match update ${update.playerId}"
            }
            playerDao.upsertRuntime(
                runtime.copy(
                    energy = update.energy,
                    overall = update.overall,
                    injuryUntilEpochDay = update.injuryUntilEpochDay,
                )
            )
        }
    }

    private suspend fun persistPlayerClubSeasonStatUpdates(
        result: CareerMatchRuntimeResult,
        updates: List<CareerMatchPlayerClubSeasonStatUpdate>,
    ) {
        val playerDao = database.careerPlayerRuntimeDao()
        updates.forEach { update ->
            requireMatchPlayer(result, update.playerId)
            requireNotNull(playerDao.findRuntime(result.state.id, update.playerId)) {
                "Missing career player runtime for club-season update ${update.playerId}"
            }
            playerDao.upsertClubSeasonStat(
                CareerPlayerClubSeasonStatEntity(
                    careerId = result.state.id,
                    playerId = update.playerId,
                    legacySeasonId = update.legacySeasonId,
                    legacyClubId = update.legacyClubId,
                    legacyC = update.legacyC,
                    legacyD = update.legacyD,
                    legacyE = update.legacyE,
                    legacyF = update.legacyF,
                    legacyG = update.legacyG,
                    legacyH = update.legacyH,
                )
            )
        }
    }

    private suspend fun requireMatchPlayer(result: CareerMatchRuntimeResult, playerId: String) {
        val membership = requireNotNull(
            database.careerPlayerRuntimeDao().findMembership(result.state.id, playerId)
        ) {
            "Missing career squad membership for match update $playerId"
        }
        val allowedClubIds = setOf(result.match.homeClubId, result.match.awayClubId)
        require(membership.clubId in allowedClubIds) {
            "Player $playerId does not belong to resolved match clubs"
        }
    }

    private suspend fun requireCareerOwner(careerId: String) {
        if (database.careerMetadataDao().findById(careerId) == null) {
            throw CareerIntegrityException("Career metadata $careerId must exist before match schedule")
        }
    }

    private fun validateSchedule(state: CareerState, schedule: List<ScheduledCareerMatch>) {
        require(schedule.map { it.matchId }.distinct().size == schedule.size) {
            "Scheduled match ids must be unique"
        }
        schedule.forEach { event ->
            require(event.matchId.isNotBlank())
            require(event.dayIndex in 0 until state.calendar.dayCount)
            require(event.eventTypeCode > 0)
            require(event.homeClubId.isNotBlank() && event.awayClubId.isNotBlank())
            require(event.homeClubId != event.awayClubId)
        }
    }

    private fun validateResolvedMatch(result: CareerMatchRuntimeResult) {
        val scheduled = result.schedule.singleOrNull { it.matchId == result.match.id }
            ?: throw IllegalArgumentException("Resolved match ${result.match.id} must exist exactly once")
        require(result.match.homeClubId == scheduled.homeClubId)
        require(result.match.awayClubId == scheduled.awayClubId)
        require(result.match.homeGoals != null && result.match.homeGoals >= 0)
        require(result.match.awayGoals != null && result.match.awayGoals >= 0)
    }

    private fun validatePlayerRuntimeUpdates(updates: List<CareerMatchPlayerRuntimeUpdate>) {
        require(updates.map { it.playerId }.distinct().size == updates.size) {
            "Match player update ids must be unique"
        }
        updates.forEach { update ->
            require(update.playerId.isNotBlank()) { "Match player id must not be blank" }
            require(update.energy >= 0) { "Match energy must not be negative for ${update.playerId}" }
            require(update.overall >= 0) { "Match overall must not be negative for ${update.playerId}" }
            require(update.injuryUntilEpochDay >= 0L) {
                "Match injury deadline must not be negative for ${update.playerId}"
            }
        }
    }

    private fun validatePlayerClubSeasonStatUpdates(updates: List<CareerMatchPlayerClubSeasonStatUpdate>) {
        val keys = updates.map { Triple(it.playerId, it.legacySeasonId, it.legacyClubId) }
        require(keys.distinct().size == keys.size) { "Match club-season stat update keys must be unique" }
        updates.forEach { update ->
            require(update.playerId.isNotBlank()) { "Match club-season player id must not be blank" }
            require(update.legacySeasonId >= 0) { "Legacy season id must not be negative" }
            require(update.legacyClubId >= 0) { "Legacy club id must not be negative" }
            require(
                listOf(
                    update.legacyC, update.legacyD, update.legacyE,
                    update.legacyF, update.legacyG, update.legacyH,
                ).all { it >= 0 }
            ) { "Legacy player club-season counters must not be negative" }
        }
    }

    private fun requireImmutableIdentity(
        entity: CareerScheduledMatchEntity,
        scheduled: ScheduledCareerMatch,
    ) {
        require(entity.dayIndex == scheduled.dayIndex)
        require(entity.eventTypeCode == scheduled.eventTypeCode)
        require(entity.homeClubId == scheduled.homeClubId)
        require(entity.awayClubId == scheduled.awayClubId)
    }

    private fun ScheduledCareerMatch.toEntity(careerId: String) = CareerScheduledMatchEntity(
        careerId = careerId,
        matchId = matchId,
        dayIndex = dayIndex,
        eventTypeCode = eventTypeCode,
        homeClubId = homeClubId,
        awayClubId = awayClubId,
        processed = processed,
        homeGoals = null,
        awayGoals = null,
    )

    private fun CareerScheduledMatchEntity.toScheduledMatch() = ScheduledCareerMatch(
        matchId = matchId,
        dayIndex = dayIndex,
        eventTypeCode = eventTypeCode,
        homeClubId = homeClubId,
        awayClubId = awayClubId,
        processed = processed,
    )

    private fun CareerScheduledMatchEntity.toMatch() = Match(
        id = matchId,
        homeClubId = homeClubId,
        awayClubId = awayClubId,
        homeGoals = homeGoals,
        awayGoals = awayGoals,
    )
}
