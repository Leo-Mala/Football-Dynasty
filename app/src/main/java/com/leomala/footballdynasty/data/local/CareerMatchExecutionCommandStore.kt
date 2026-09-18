package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeResult
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult
import com.leomala.footballdynasty.domain.manager.LegacyTacticsRawState

/**
 * Productive Phase 17 command boundary for a manager-controlled match.
 *
 * Persistence, ticket finance, career RNG restoration and atomic commit remain owned by
 * [CareerMatchExecutionCoordinator]. This boundary only supplies the certified runtime composition.
 */
class CareerMatchExecutionCommandStore(
    private val coordinator: CareerMatchExecutionCoordinator,
    private val composition: CareerMatchRuntimeComposition,
) {
    constructor(
        database: FootballDynastyDatabase,
        composition: CareerMatchRuntimeComposition,
    ) : this(CareerMatchExecutionCoordinator(database), composition)

    data class Command(
        val careerId: String,
        val matchId: String,
        val homeLineup: LegacyLineupCommitResult<String>,
        val awayLineup: LegacyLineupCommitResult<String>,
        val homeTactics: LegacyTacticsRawState,
        val awayTactics: LegacyTacticsRawState,
        val homeSubstitutionsRemaining: Int,
        val awaySubstitutionsRemaining: Int,
        val homeLegacyModeFlag: Boolean,
        val awayLegacyModeFlag: Boolean,
        val includeTicketFinance: Boolean = true,
    )

    val runtimeCompositionAvailable: Boolean
        get() = composition.available

    suspend fun execute(command: Command): CareerMatchRuntimeResult {
        require(runtimeCompositionAvailable) {
            "Productive legacy match runtime composition is not wired"
        }
        return coordinator.executeManagerMatch(
            careerId = command.careerId,
            matchId = command.matchId,
            homeLineup = command.homeLineup,
            awayLineup = command.awayLineup,
            homeTactics = command.homeTactics,
            awayTactics = command.awayTactics,
            homeSubstitutionsRemaining = command.homeSubstitutionsRemaining,
            awaySubstitutionsRemaining = command.awaySubstitutionsRemaining,
            homeLegacyModeFlag = command.homeLegacyModeFlag,
            awayLegacyModeFlag = command.awayLegacyModeFlag,
            includeTicketFinance = command.includeTicketFinance,
        ) { scheduled, state, homeTacticIndex, awayTacticIndex, random ->
            composition.simulate(
                scheduled = scheduled,
                state = state,
                homeTacticIndex = homeTacticIndex,
                awayTacticIndex = awayTacticIndex,
                random = random,
            )
        }
    }
}
