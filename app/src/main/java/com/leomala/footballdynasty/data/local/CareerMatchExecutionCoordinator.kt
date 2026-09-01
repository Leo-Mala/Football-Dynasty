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

/** Fully resolved ticket input. V9 owns every mutable/class-identity field consumed by calculation. */
data class CareerMatchTicketRuntimeInput(
    val calculation: LegacyTicketCalculationInput,
    val homeLegacyQ0: Boolean,
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
    private val atomicCommitter = CareerMatchAtomicCommitter(database, clockMillis)

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
        includeTicketFinance: Boolean = false,
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
     * effects, calendar and advanced RNG are committed by [CareerMatchAtomicCommitter] atomically.
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

            val match = simulate(event, transientState, random)

            if (grossTicketIncome != null) {
                val ticket = requireNotNull(ticketRuntimeInput)
                financeAfter = LegacyTicketFinanceRule.applyHomeTicketIncome(
                    state = requireNotNull(financeBefore),
                    rawCompetitionType = ticket.calculation.rawCompetitionType,
                    homeLegacyQ0 = ticket.homeLegacyQ0,
                    grossTicketIncome = grossTicketIncome,
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
