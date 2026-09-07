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
import com.leomala.footballdynasty.domain.match.LegacyMatchRatingMetricRuntimeRules
import com.leomala.footballdynasty.domain.match.LegacyMatchRatingParticipantRuntime
import com.leomala.footballdynasty.domain.model.Match
import com.leomala.footballdynasty.foundation.random.FreshJavaRandomSource
import com.leomala.footballdynasty.foundation.random.RandomSource

/** Fully resolved ticket input. V9 owns every mutable/class-identity field consumed by calculation. */
data class CareerMatchTicketRuntimeInput(
    val calculation: LegacyTicketCalculationInput,
    val homeLegacyQ0: Boolean,
)

/**
 * Exact extra output required to execute the recovered post-simulation `best.s.e()` rating pass.
 *
 * [ratingMetricState] is the match-local `best.s` counter projection produced by the simulation.
 * It is deliberately supplied by the simulator rather than reconstructed from score/events later.
 */
data class CareerMatchRatedSimulationResult(
    val match: Match,
    val ratingMetricState: LegacyMatchRatingMetricRuntimeRules.State,
)

/** End-to-end persisted match execution seam around the certified Phase 8 runtime. */
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
    private val coachPostMatchResolver = CareerCoachPostMatchPersistedResolver(database)
    private val atomicCommitter = CareerMatchAtomicCommitter(database, clockMillis)

    private data class InternalSimulationResult(
        val match: Match,
        val ratingMetricState: LegacyMatchRatingMetricRuntimeRules.State? = null,
    )

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
        includeTicketFinance: Boolean = true,
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
        includeTicketFinance = includeTicketFinance,
    ) { scheduled, state, random ->
        simulate(
            scheduled,
            state,
            LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(homeTactics),
            LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(awayTactics),
            random,
        )
    }

    /**
     * Manager-facing rated path. The simulator returns its exact match-local rating metrics; this
     * boundary captures original starters before simulation and performs the recovered `best.s.e()`
     * pass afterward without consuming the career RNG.
     */
    suspend fun executeManagerMatchWithRatings(
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
        includeTicketFinance: Boolean = true,
        implicitRatingRandomFactory: () -> RandomSource = { FreshJavaRandomSource() },
        simulate: (
            scheduled: ScheduledCareerMatch,
            state: PersistedState,
            homeTacticIndex: Int,
            awayTacticIndex: Int,
            random: RandomSource,
        ) -> CareerMatchRatedSimulationResult,
    ): CareerMatchRuntimeResult = executeWithRatings(
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
        includeTicketFinance = includeTicketFinance,
        implicitRatingRandomFactory = implicitRatingRandomFactory,
    ) { scheduled, state, random ->
        simulate(
            scheduled,
            state,
            LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(homeTactics),
            LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(awayTactics),
            random,
        )
    }

    suspend fun execute(
        careerId: String,
        matchId: String,
        homeLineup: LegacyLineupCommitResult<String>,
        awayLineup: LegacyLineupCommitResult<String>,
        homeSubstitutionsRemaining: Int,
        awaySubstitutionsRemaining: Int,
        homeLegacyModeFlag: Boolean,
        awayLegacyModeFlag: Boolean,
        includeTicketFinance: Boolean = false,
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
        includeTicketFinance = includeTicketFinance,
        simulate = simulate,
    )

    /**
     * Low-level seam retained for exact transient-state characterization and specialized callers.
     *
     * When [includeTicketFinance] is true, every ticket input is resolved from persisted V9/source
     * state. Legacy `best.s.Q0()` performs stadium attendance before its later match RNG sites, so
     * ticket calculation consumes the exact career [RandomSource] before [simulate]. The gross is
     * credited only after simulation, matching the later `best.s.h()` step. Finance, score, player
     * effects, type-7 coach post-match state, calendar and advanced RNG are committed by
     * [CareerMatchAtomicCommitter] atomically.
     */
    suspend fun execute(
        careerId: String,
        matchId: String,
        transientEvidence: CareerMatchPersistedRuntimeResolver.TransientMatchEvidence,
        includeTicketFinance: Boolean = false,
        simulate: (
            scheduled: ScheduledCareerMatch,
            state: PersistedState,
            random: RandomSource,
        ) -> Match,
    ): CareerMatchRuntimeResult = executeInternal(
        careerId = careerId,
        matchId = matchId,
        transientEvidence = transientEvidence,
        includeTicketFinance = includeTicketFinance,
        implicitRatingRandomFactory = null,
    ) { scheduled, state, random ->
        InternalSimulationResult(match = simulate(scheduled, state, random))
    }

    /**
     * Rated low-level seam. Original starters are captured before [simulate], then the exact metric
     * state returned by [simulate] drives `best.o.n(...)` in legacy `u0 -> v0 -> F -> G` order.
     * The fresh implicit rating RNG is separate from the persisted career RNG by construction.
     */
    suspend fun executeWithRatings(
        careerId: String,
        matchId: String,
        transientEvidence: CareerMatchPersistedRuntimeResolver.TransientMatchEvidence,
        includeTicketFinance: Boolean = false,
        implicitRatingRandomFactory: () -> RandomSource = { FreshJavaRandomSource() },
        simulate: (
            scheduled: ScheduledCareerMatch,
            state: PersistedState,
            random: RandomSource,
        ) -> CareerMatchRatedSimulationResult,
    ): CareerMatchRuntimeResult = executeInternal(
        careerId = careerId,
        matchId = matchId,
        transientEvidence = transientEvidence,
        includeTicketFinance = includeTicketFinance,
        implicitRatingRandomFactory = implicitRatingRandomFactory,
    ) { scheduled, state, random ->
        simulate(scheduled, state, random).let { resolved ->
            InternalSimulationResult(
                match = resolved.match,
                ratingMetricState = resolved.ratingMetricState,
            )
        }
    }

    private suspend fun executeInternal(
        careerId: String,
        matchId: String,
        transientEvidence: CareerMatchPersistedRuntimeResolver.TransientMatchEvidence,
        includeTicketFinance: Boolean,
        implicitRatingRandomFactory: (() -> RandomSource)?,
        simulate: (
            scheduled: ScheduledCareerMatch,
            state: PersistedState,
            random: RandomSource,
        ) -> InternalSimulationResult,
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
        val ratingParticipantSnapshot = implicitRatingRandomFactory?.let {
            LegacyMatchRatingParticipantRuntime.capture(transientState)
        }
        val matchDate = LegacyCalendarRules.dateAt(
            state.calendar.copy(currentDayIndex = scheduled.dayIndex)
        )

        val ticketRuntimeInput = if (includeTicketFinance) {
            ticketInputResolver.resolve(careerId, scheduled)
        } else {
            null
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
        var ratingMetricState: LegacyMatchRatingMetricRuntimeRules.State? = null

        val result = CareerMatchRuntimeBridge.run(
            state = state,
            schedule = schedule,
            matchId = matchId,
        ) { event, random ->
            require(event == scheduled) { "Career bridge changed scheduled match identity" }

            val grossTicketIncome = ticketRuntimeInput?.let { ticket ->
                LegacyTicketFinanceRule.calculate(
                    input = ticket.calculation.copy(
                        capacities = requireNotNull(stadiumBefore).capacities,
                    ),
                    random = random,
                ).grossTicketIncome
            }

            val simulation = simulate(event, transientState, random)
            ratingMetricState = simulation.ratingMetricState

            if (grossTicketIncome != null) {
                val ticket = requireNotNull(ticketRuntimeInput)
                financeAfter = LegacyTicketFinanceRule.applyHomeTicketIncome(
                    state = requireNotNull(financeBefore),
                    rawCompetitionType = ticket.calculation.rawCompetitionType,
                    homeLegacyQ0 = ticket.homeLegacyQ0,
                    grossTicketIncome = grossTicketIncome,
                )
            }
            simulation.match
        }

        val competitionPlayerRatingMutations = implicitRatingRandomFactory?.let { factory ->
            CareerMatchPlayerRatingRuntimeExecutor.execute(
                state = transientState,
                participantSnapshot = requireNotNull(ratingParticipantSnapshot) {
                    "Rated match execution must capture original participants before simulation"
                },
                metricState = requireNotNull(ratingMetricState) {
                    "Rated match simulation must return exact legacy rating metric state"
                },
                implicitRandomFactory = factory,
            ).map { rated -> rated.toCompetitionMutation() }
        }.orEmpty()

        val coachUpdates = coachPostMatchResolver.resolveTypeSeven(
            careerId = careerId,
            scheduled = scheduled,
            seasonId = state.season.number,
            homeGoals = requireNotNull(result.match.homeGoals),
            awayGoals = requireNotNull(result.match.awayGoals),
        )

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
            coachUpdatesInLegacyOrder = coachUpdates,
            competitionPlayerRatingMutationsInLegacyOrder = competitionPlayerRatingMutations,
        )
        return result
    }
}
