package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeBridge
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeResult
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult
import com.leomala.footballdynasty.domain.manager.LegacyTacticsMatchRuntimeRule
import com.leomala.footballdynasty.domain.manager.LegacyTacticsRawState
import com.leomala.footballdynasty.domain.manager.LegacyTicketCalculationInput
import com.leomala.footballdynasty.domain.manager.LegacyTicketFinanceRule
import com.leomala.footballdynasty.domain.model.Match
import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Resolved ticket input after persisted competition/club/Q0 fields and the remaining explicitly
 * characterized career-mutable evidence have been joined.
 *
 * The coordinator still replaces [LegacyTicketCalculationInput.capacities] with the durable career
 * stadium sectors before calculation, so no caller can bypass the fail-closed V8 state.
 */
data class CareerMatchTicketRuntimeInput(
    val calculation: LegacyTicketCalculationInput,
    val homeLegacyQ0: Boolean,
)

/**
 * End-to-end Phase 9 persistence seam around the certified Phase 8 runtime.
 *
 * Phase 11 now supplies the previously unresolved lineup-only `g0` state through the characterized
 * ActivityEscalacao.y()/B() commit result. The manager execution path also carries the raw tactic
 * index proven by `best.s.k(...)` (`best.c0.i0()[2]`). The lower-level explicit transient-evidence
 * overload remains available for characterization and specialized callers. All proven post-match
 * effects are committed atomically with score, calendar and career RNG.
 */
class CareerMatchExecutionCoordinator(
    database: FootballDynastyDatabase,
    clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val stateRepository = RoomCareerStateRepository(database)
    private val store = CareerMatchStore(database, clockMillis)
    private val resolver = CareerMatchPersistedRuntimeResolver(database)
    private val managerStore = CareerManagerRuntimeStore(database)
    private val stadiumStore = CareerStadiumRuntimeStore(database)
    private val ticketInputResolver = CareerMatchTicketInputResolver(database)
    private val atomicCommitter = CareerMatchAtomicCommitter(database, clockMillis)

    /**
     * Complete manager entry point for characterized pre-match lineup + tactics state.
     * The callback receives the exact raw tactic indexes consumed by the legacy match engine.
     */
    suspend fun executeManagerMatch(
        careerId: String,
        matchId: String,
        homeLineup: LegacyLineupCommitResult<String>,
        awayLineup: LegacyLineupCommitResult<String>,
        homeTactics: LegacyTacticsRawState,
        awayTactics: LegacyTacticsRawState,
        homeSubstitutionsRemaining: Int,
        awaySubstitutionsRemaining: Int,
        homeLegacyModeFlag: Boolean,
        awayLegacyModeFlag: Boolean,
        simulate: (
            scheduled: ScheduledCareerMatch,
            state: PersistedState,
            homeTacticIndex: Int,
            awayTacticIndex: Int,
            random: RandomSource,
        ) -> Match,
    ): CareerMatchRuntimeResult = execute(
        careerId = careerId,
        matchId = matchId,
        homeLineup = homeLineup,
        awayLineup = awayLineup,
        homeSubstitutionsRemaining = homeSubstitutionsRemaining,
        awaySubstitutionsRemaining = awaySubstitutionsRemaining,
        homeLegacyModeFlag = homeLegacyModeFlag,
        awayLegacyModeFlag = awayLegacyModeFlag,
    ) { scheduled, state, random ->
        simulate(
            scheduled,
            state,
            LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(homeTactics),
            LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(awayTactics),
            random,
        )
    }

    /** Normal lineup-aware path when tactics are owned by a more specialized match caller. */
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

    /**
     * Low-level seam retained for exact transient-state characterization and specialized callers.
     *
     * When [ticketEvidence] is present, [CareerMatchTicketInputResolver] first obtains competition
     * type, p0, J and Q0 from persisted/source state. `best.k.b(best.s)` then runs after match
     * simulation on the exact same career [RandomSource]. Its finance mutation is committed by
     * [CareerMatchAtomicCommitter] in the same Room transaction as score, player effects, calendar
     * progression and the advanced RNG state.
     */
    suspend fun execute(
        careerId: String,
        matchId: String,
        transientEvidence: CareerMatchPersistedRuntimeResolver.TransientMatchEvidence,
        ticketEvidence: CareerMatchTicketUnpersistedEvidence? = null,
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

        val ticketRuntimeInput = ticketEvidence?.let {
            ticketInputResolver.resolve(careerId, scheduled, it)
        }
        val financeBefore: LegacyFinanceRuntimeState? = ticketRuntimeInput?.let {
            requireNotNull(managerStore.clubFinanceState(careerId, scheduled.homeClubId)) {
                "Missing materialized home finance state $careerId/${scheduled.homeClubId}"
            }
        }
        val stadiumBefore: CareerStadiumRuntimeState? = ticketRuntimeInput?.let {
            requireNotNull(stadiumStore.find(careerId, scheduled.homeClubId)) {
                "Missing materialized four-sector stadium state $careerId/${scheduled.homeClubId}"
            }
        }
        var financeAfter: LegacyFinanceRuntimeState? = null

        val result = CareerMatchRuntimeBridge.run(
            state = state,
            schedule = schedule,
            matchId = matchId,
        ) { event, random ->
            require(event == scheduled) { "Career bridge changed scheduled match identity" }
            val match = simulate(event, transientState, random)
            ticketRuntimeInput?.let { ticket ->
                val calculation = LegacyTicketFinanceRule.calculate(
                    input = ticket.calculation.copy(
                        capacities = requireNotNull(stadiumBefore).capacities,
                    ),
                    random = random,
                )
                financeAfter = LegacyTicketFinanceRule.applyHomeTicketIncome(
                    state = requireNotNull(financeBefore),
                    rawCompetitionType = ticket.calculation.rawCompetitionType,
                    homeLegacyQ0 = ticket.homeLegacyQ0,
                    grossTicketIncome = calculation.grossTicketIncome,
                )
            }
            match
        }

        atomicCommitter.commit(
            result = result,
            playerRuntimeUpdates = CareerMatchPersistedEffectsMapper.playerRuntimeUpdates(
                transientState,
                matchDate,
            ),
            playerClubSeasonStatUpdates =
                CareerMatchPersistedEffectsMapper.playerClubSeasonStatUpdates(transientState),
            financeUpdate = financeAfter?.let { after ->
                CareerMatchFinanceUpdate(
                    clubId = scheduled.homeClubId,
                    expectedBefore = requireNotNull(financeBefore),
                    after = after,
                )
            },
        )
        return result
    }
}
