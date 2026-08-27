package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.domain.career.CareerIntegrityValidator
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeResult
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.model.Match
import com.leomala.footballdynasty.foundation.error.CareerIntegrityException

/**
 * Atomic Room boundary for Phase 9 scheduled-match + career RNG/calendar persistence.
 *
 * The table stores only already-proven scheduled event identity and the resolved modern score.
 * Competition tables, standings and round-generation semantics remain Phase 10 concerns.
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

    /** Commits post-match career state and resolved score in one Room transaction. */
    suspend fun commitMatch(result: CareerMatchRuntimeResult) {
        CareerIntegrityValidator.validate(result.state)
        validateSchedule(result.state, result.schedule)
        validateResolvedMatch(result)

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
