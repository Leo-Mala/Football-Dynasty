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
 * [ratingMetricState] is supplied by the simulator rather than reconstructed. [tieBreakMutation]
 * likewise carries raw `best.s.N0()/A0()` only when the simulator actually reached that legacy path.
 */
data class CareerMatchRatedSimulationResult(
    val match: Match,
    val ratingMetricState: LegacyMatchRatingMetricRuntimeRules.State,
    val tieBreakMutation: CareerMatchTieBreakMutation? = null,
)

/** End-to-end persisted match execution seam around the certified Phase 8 runtime. */
class CareerMatchExecutionCoordinator(
    private val database: FootballDynastyDatabase,
    private val roundTransientRatingRuntime: CareerLeagueRoundTransientRatingRuntime =
        CareerLeagueRoundTransientRatingRuntime(),
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
        val tieBreakMutation: CareerMatchTieBreakMutation? = null,
    )

    private data class TransientLeagueOwner(
        val competitionId: String,
        val roundNumber: Int,
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
                tieBreakMutation = resolved.tieBreakMutation,
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
        val transientLeagueOwner = if (implicitRatingRandomFactory != null) {
            resolveTransientLeagueOwner(careerId, matchId)
        } else {
            null
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
        var tieBreakMutation: CareerMatchTieBreakMutation? = null

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
            tieBreakMutation = simulation.tieBreakMutation

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

        val ratedPlayers = implicitRatingRandomFactory?.let { factory ->
            CareerMatchPlayerRatingRuntimeExecutor.execute(
                state = transientState,
                participantSnapshot = requireNotNull(ratingParticipantSnapshot) {
                    "Rated match execution must capture original participants before simulation"
                },
                metricState = requireNotNull(ratingMetricState) {
                    "Rated match simulation must return exact legacy rating metric state"
                },
                implicitRandomFactory = factory,
                captureTransientLeague = transientLeagueOwner != null,
            )
        }.orEmpty()
        val competitionPlayerRatingMutations = ratedPlayers.map { it.toCompetitionMutation() }
        val playerMatchRatingHistoryMutations = ratedPlayers.map { rated ->
            CareerPlayerMatchRatingHistoryMutation(
                playerId = rated.playerId,
                ratingY0 = rated.ratingY0,
            )
        }

        val stagedRound = transientLeagueOwner?.let { owner ->
            roundTransientRatingRuntime.stage(
                key = CareerLeagueRoundTransientRatingRuntime.Key(
                    careerId = careerId,
                    competitionId = owner.competitionId,
                    roundNumber = owner.roundNumber,
                ),
                capturesInLegacyOrder = ratedPlayers.mapNotNull { rated ->
                    rated.transientLeagueCapture?.let { capture ->
                        CareerLeagueRoundTransientRatingRuntime.CaptureInput(
                            playerId = rated.playerId,
                            clubIdAtSnapshot = when (rated.side) {
                                0 -> scheduled.homeClubId
                                1 -> scheduled.awayClubId
                                else -> error("Legacy rating side must be 0 or 1")
                            },
                            ratingY0 = capture.ratingY0,
                            legacyG0 = capture.legacyG0,
                            randomOrder = capture.randomOrder,
                        )
                    }
                },
            )
        }

        val coachUpdates = coachPostMatchResolver.resolveTypeSeven(
            careerId = careerId,
            scheduled = scheduled,
            seasonId = state.season.number,
            homeGoals = requireNotNull(result.match.homeGoals),
            awayGoals = requireNotNull(result.match.awayGoals),
        )

        val commitOutcome = atomicCommitter.commit(
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
            playerMatchRatingHistoryMutationsInLegacyOrder = playerMatchRatingHistoryMutations,
            tieBreakMutation = tieBreakMutation,
            roundSnapshotStaging = stagedRound?.let { staged ->
                CareerRoundSnapshotStaging(
                    competitionId = staged.key.competitionId,
                    roundNumber = staged.key.roundNumber,
                    legacySeasonIndex = state.season.number,
                    candidatesInLegacyOrder = staged.candidatesInLegacyOrder,
                )
            },
        )

        // Only now may the process-local z2 buffer advance/clear: any exception above rolled the Room
        // transaction back, so mutating transient state earlier would diverge from durable career state.
        stagedRound?.let { staged ->
            val roundClosed =
                commitOutcome.advancedCompetitionId == staged.key.competitionId &&
                    commitOutcome.advancedRoundNumber == staged.key.roundNumber
            roundTransientRatingRuntime.commit(staged, roundClosed)
        }
        return result
    }

    /**
     * `career_competitions` is the modern owner for the proven `konrent.t` league subset. The
     * legacy z2 path additionally requires `k0.E()==1`, represented by legacyCompetitionType == 1.
     */
    private suspend fun resolveTransientLeagueOwner(
        careerId: String,
        matchId: String,
    ): TransientLeagueOwner? {
        val dao = database.careerCompetitionDao()
        val links = dao.matchLinksForMatch(careerId, matchId)
        if (links.isEmpty()) return null
        require(links.size == 1) {
            "Legacy match $careerId/$matchId must resolve zero or one competition, found ${links.size}"
        }
        val link = links.single()
        val competition = requireNotNull(dao.findCompetition(careerId, link.competitionId)) {
            "Missing competition ${link.competitionId} linked to match $careerId/$matchId"
        }
        require(link.roundNumber == competition.currentRoundNumber) {
            "Rated match $matchId does not belong to current competition round"
        }
        if (competition.legacyCompetitionType != 1) return null
        return TransientLeagueOwner(
            competitionId = link.competitionId,
            roundNumber = link.roundNumber,
        )
    }
}
