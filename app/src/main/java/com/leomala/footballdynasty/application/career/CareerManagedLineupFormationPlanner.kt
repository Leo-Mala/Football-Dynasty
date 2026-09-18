package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitRule
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitSlot
import com.leomala.footballdynasty.domain.manager.LegacyLineupFormationRuntimeRule
import com.leomala.footballdynasty.domain.manager.LegacyLineupFormationTables
import com.leomala.footballdynasty.domain.manager.LegacyLineupMatchLists
import com.leomala.footballdynasty.domain.manager.LegacyLineupPlayerWrite
import com.leomala.footballdynasty.domain.manager.LegacyLineupPreparedState
import com.leomala.footballdynasty.domain.manager.LegacyLineupRuntimePlayer

/**
 * Phase 17 application boundary that turns an explicit manager formation choice into the exact
 * characterized ActivityEscalacao formation state.
 *
 * This planner deliberately owns no default formation and never derives formation from tactics.
 * A missing/invalid selection remains blocked. Eligibility is projected through the already
 * recovered K0 boundary; unresolved eligibility also remains blocked instead of becoming false.
 *
 * The legacy ActivityMainTeam-presence bit required by ActivityEscalacao.y() is intentionally an
 * explicit argument of [commit]. The modern Compose screen is not silently treated as that legacy
 * activity owner.
 */
object CareerManagedLineupFormationPlanner {
    enum class Blocker {
        NO_MANAGED_MATCH,
        FORMATION_NOT_SELECTED,
        INVALID_FORMATION,
        LINEUP_ELIGIBILITY_UNRESOLVED,
    }

    data class Prepared(
        val managedSide: CareerLineupInputCatalogStore.ManagedMatchSide,
        val formationIndex: Int,
        val state: LegacyLineupPreparedState<CareerLineupInputCatalogStore.PlayerInput>,
        internal val eligibleRoster: List<LegacyLineupRuntimePlayer<CareerLineupInputCatalogStore.PlayerInput>>,
    )

    data class Resolution(
        val prepared: Prepared? = null,
        val blocker: Blocker? = null,
    ) {
        init {
            require((prepared == null) xor (blocker == null)) {
                "Formation resolution must be either prepared or blocked"
            }
        }

        val ready: Boolean
            get() = prepared != null
    }

    val availableFormationIndices: List<Int> =
        LegacyLineupFormationTables.formationSlots.indices.toList()

    fun prepare(
        inputs: CareerLineupInputCatalogStore.LineupInputs,
        formationIndex: Int?,
    ): Resolution {
        val preparation = inputs.matchPreparation
        val managedSide = preparation.managedSide
        if (preparation.matchId == null || managedSide == null) {
            return Resolution(blocker = Blocker.NO_MANAGED_MATCH)
        }
        if (formationIndex == null) {
            return Resolution(blocker = Blocker.FORMATION_NOT_SELECTED)
        }
        if (formationIndex !in availableFormationIndices) {
            return Resolution(blocker = Blocker.INVALID_FORMATION)
        }

        val projectedEligibility = inputs.players.map { player ->
            CareerLineupEligibilityProjection.resolveWithoutCareerClock(
                player = player,
                preparation = preparation,
            )
        }
        if (projectedEligibility.any { it == null }) {
            return Resolution(blocker = Blocker.LINEUP_ELIGIBILITY_UNRESOLVED)
        }

        val runtimePlayers = inputs.players.map { player ->
            LegacyLineupRuntimePlayer(
                value = player,
                positionCode = player.positionCode,
                sideCode = player.sideCode,
                subroleCode = player.subroleCode,
                skill = player.skill,
                energy = player.energy,
                star = player.star,
            )
        }
        val eligibleRoster = runtimePlayers.filterIndexed { index, _ -> projectedEligibility[index] == true }
        val unavailableRoster = runtimePlayers.filterIndexed { index, _ -> projectedEligibility[index] == false }
        val prepared = LegacyLineupFormationRuntimeRule.buildAutomatic(
            formationIndex = formationIndex,
            eligibleRoster = eligibleRoster,
            unavailableRoster = unavailableRoster,
        )

        return Resolution(
            prepared = Prepared(
                managedSide = managedSide,
                formationIndex = formationIndex,
                state = prepared,
                eligibleRoster = eligibleRoster,
            )
        )
    }

    /**
     * Applies the exact characterized commit rule only after every input owner is explicit.
     * The returned command payload is reduced to stable player ids without changing list order,
     * lineup codes or starter-write semantics.
     */
    fun commit(
        prepared: Prepared,
        legacyMainTeamActivityPresent: Boolean,
    ): LegacyLineupCommitResult<String> {
        val sideIndex = when (prepared.managedSide) {
            CareerLineupInputCatalogStore.ManagedMatchSide.HOME -> 0
            CareerLineupInputCatalogStore.ManagedMatchSide.AWAY -> 1
        }
        val committed = LegacyLineupCommitRule.commit(
            starterSlots = prepared.state.starters.map { slot ->
                LegacyLineupCommitSlot(
                    player = slot.player?.value,
                    slotCode = slot.slotCode,
                )
            },
            benchPlayers = prepared.state.bench.map { it.value },
            eligibleRoster = prepared.eligibleRoster.map { it.value },
            matchSideIndex = sideIndex,
            mainTeamActivityPresent = legacyMainTeamActivityPresent,
        )

        return LegacyLineupCommitResult(
            clubStarters = committed.clubStarters.map { it.playerId },
            clubBench = committed.clubBench.map { it.playerId },
            playerWrites = committed.playerWrites.map { write ->
                LegacyLineupPlayerWrite(
                    player = write.player.playerId,
                    lineupCode = write.lineupCode,
                    starterFlagWrite = write.starterFlagWrite,
                )
            },
            matchLists = LegacyLineupMatchLists(
                sideIndex = committed.matchLists.sideIndex,
                replaceSideLists = committed.matchLists.replaceSideLists,
                startersPrimary = committed.matchLists.startersPrimary.map { it.playerId },
                startersMirror = committed.matchLists.startersMirror.map { it.playerId },
                bench = committed.matchLists.bench.map { it.playerId },
            ),
            finalizePlan = committed.finalizePlan,
        )
    }
}
