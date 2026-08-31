package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeBridge
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeResult
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult
import com.leomala.footballdynasty.domain.model.Match
import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * End-to-end Phase 9 persistence seam around the certified Phase 8 runtime.
 *
 * Phase 11 now supplies the previously unresolved lineup-only `g0` state through the characterized
 * ActivityEscalacao.y()/B() commit result. The lower-level explicit transient-evidence overload is
 * retained for characterization and specialized callers. All proven post-match effects are committed
 * atomically with score, calendar and career RNG.
 */
class CareerMatchExecutionCoordinator(
    database: FootballDynastyDatabase,
    clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val stateRepository = RoomCareerStateRepository(database)
    private val store = CareerMatchStore(database, clockMillis)
    private val resolver = CareerMatchPersistedRuntimeResolver(database)

    /** Normal manager path: characterized lineups feed the persisted match bridge directly. */
    suspend fun execute(
        careerId: String,
        matchId: String,
        homeLineup: LegacyLineupCommitResult<String>,
        awayLineup: LegacyLineupCommitResult<String>,
        homeSubstitutionsRemaining: Int,
        awaySubstitutionsRemaining: Int,
        homeLegacyModeFlag: Boolean,
        awayLegacyModeFlag: Boolean,
        simulate: (
            scheduled: ScheduledCareerMatch,
            state: PersistedState,
            random: RandomSource,
        ) -> Match,
    ): CareerMatchRuntimeResult = execute(
        careerId = careerId,
        matchId = matchId,
        transientEvidence = CareerLineupMatchEvidenceMapper.fromLineups(
            home = homeLineup,
            away = awayLineup,
            homeSubstitutionsRemaining = homeSubstitutionsRemaining,
            awaySubstitutionsRemaining = awaySubstitutionsRemaining,
            homeLegacyModeFlag = homeLegacyModeFlag,
            awayLegacyModeFlag = awayLegacyModeFlag,
        ),
        simulate = simulate,
    )

    /** Low-level seam retained for exact transient-state characterization and specialized callers. */
    suspend fun execute(
        careerId: String,
        matchId: String,
        transientEvidence: CareerMatchPersistedRuntimeResolver.TransientMatchEvidence,
        simulate: (
            scheduled: ScheduledCareerMatch,
            state: PersistedState,
            random: RandomSource,
        ) -> Match,
    ): CareerMatchRuntimeResult {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        require(matchId.isNotBlank()) { "Match id must not be blank" }

        val state = requireNotNull(stateRepository.findById(careerId)) {
            "Missing persisted career state for $careerId"
        }
        val schedule = store.loadSchedule(careerId)
        val scheduled = schedule.singleOrNull { it.matchId == matchId }
            ?: throw IllegalArgumentException("Scheduled match $matchId must exist exactly once")
        val roster = resolver.resolve(careerId, scheduled)
        require(roster.currentSeasonId == state.season.number) {
            "Persisted match roster season diverged from career state"
        }
        val transientState = resolver.hydratePhase8State(roster, transientEvidence)
        val matchDate = LegacyCalendarRules.dateAt(
            state.calendar.copy(currentDayIndex = scheduled.dayIndex)
        )

        val result = CareerMatchRuntimeBridge.run(
            state = state,
            schedule = schedule,
            matchId = matchId,
        ) { event, random ->
            require(event == scheduled) { "Career bridge changed scheduled match identity" }
            simulate(event, transientState, random)
        }

        store.commitMatch(
            result = result,
            playerRuntimeUpdates = CareerMatchPersistedEffectsMapper.playerRuntimeUpdates(
                transientState,
                matchDate,
            ),
            playerClubSeasonStatUpdates =
                CareerMatchPersistedEffectsMapper.playerClubSeasonStatUpdates(transientState),
        )
        return result
    }
}
