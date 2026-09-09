package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerClubTacticsStore
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.domain.career.CareerScheduleCalendarProjection
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyPlayerSubroleCodeRule
import com.leomala.footballdynasty.domain.manager.LegacyTacticsMatchRuntimeRule
import com.leomala.footballdynasty.domain.match.LegacyMatchSubstitutionRules
import java.time.LocalDate

/**
 * Read-only Phase 17 boundary for the persisted/source-proven inputs already proven to feed
 * the legacy lineup runtime.
 *
 * The boundary also exposes fail-closed readiness for the next managed match.
 * It never substitutes test fixtures or unproven defaults for unresolved legacy owners.
 */
class CareerLineupInputCatalogStore(
    private val database: FootballDynastyDatabase,
) {
    data class LineupInputs(
        val careerId: String,
        val clubId: String,
        val players: List<PlayerInput>,
        val matchPreparation: MatchPreparation,
    )

    data class PlayerInput(
        val playerId: String,
        val name: String,
        val positionCode: Int,
        val sideCode: Int,
        val subroleCode: Int,
        val skill: Int,
        val energy: Int,
        val star: Boolean,
        val sourceOrdinal: Int,
        /** Exact modern owner for legacy `best.o.M0()` against the prepared match date. */
        val blockedByM0ForPreparedMatch: Boolean?,
    )

    enum class ManagedMatchSide {
        HOME,
        AWAY,
    }

    enum class MatchPreparationBlocker {
        NO_PLAYABLE_MATCH,
        MANAGED_CLUB_NOT_ON_NEXT_PLAYABLE_DAY,
        HOME_SENIOR_ROSTER_EMPTY,
        AWAY_SENIOR_ROSTER_EMPTY,
        LINEUP_ELIGIBILITY_OWNER_UNRESOLVED,
        TACTICS_STATE_OWNER_UNRESOLVED,
        SUBSTITUTION_BUDGET_OWNER_UNRESOLVED,
        LEGACY_MODE_FLAG_OWNER_UNRESOLVED,
        MATCH_RUNTIME_COMPOSITION_UNWIRED,
    }

    data class MatchPreparation(
        val nextPlayableDayIndex: Int?,
        val matchId: String?,
        val homeClubId: String?,
        val awayClubId: String?,
        val managedSide: ManagedMatchSide?,
        val homeSeniorRosterCount: Int?,
        val awaySeniorRosterCount: Int?,
        val homeTacticIndex: Int?,
        val awayTacticIndex: Int?,
        val homeSubstitutionsRemaining: Int?,
        val awaySubstitutionsRemaining: Int?,
        val homeLegacyModeFlag: Boolean?,
        val awayLegacyModeFlag: Boolean?,
        val blockers: Set<MatchPreparationBlocker>,
    ) {
        val executable: Boolean
            get() = matchId != null && blockers.isEmpty()
    }

    internal data class TransientClubOwners(
        val homeSubstitutionsRemaining: Int,
        val awaySubstitutionsRemaining: Int,
        val homeLegacyModeFlag: Boolean,
        val awayLegacyModeFlag: Boolean,
    )

    suspend fun loadManagedClubLineupInputs(careerId: String): LineupInputs? =
        database.withTransaction {
            require(careerId.isNotBlank()) { "Career id must not be blank" }

            val coreEntity = database.careerCoreStateDao().findById(careerId)
                ?: return@withTransaction null
            val state = CareerCoreStateRoomAdapter.state(coreEntity)
            val clubId = state.managedClub?.clubId ?: return@withTransaction null
            if (database.clubDao().findById(clubId) == null) {
                return@withTransaction null
            }

            val matchPreparation = buildMatchPreparation(
                state = state,
                managedClubId = clubId,
            )
            val preparedMatchEpochDay = matchPreparation.matchId?.let {
                val dayIndex = requireNotNull(matchPreparation.nextPlayableDayIndex) {
                    "Prepared match must expose its playable day"
                }
                val gameDate = LegacyCalendarRules.dateAt(
                    state.calendar.copy(currentDayIndex = dayIndex)
                )
                LocalDate.of(gameDate.year, gameDate.month, gameDate.day).toEpochDay()
            }

            val playerRuntimeDao = database.careerPlayerRuntimeDao()
            val memberships = playerRuntimeDao.membershipsForClub(
                careerId = careerId,
                clubId = clubId,
                rosterKind = ROSTER_SENIOR,
            )
            val players = memberships.map { membership ->
                val runtime = requireNotNull(
                    playerRuntimeDao.findRuntime(careerId, membership.playerId)
                ) {
                    "Missing runtime for career=$careerId player=${membership.playerId}"
                }

                val staticInput = when (runtime.sourceType) {
                    CareerPlayerRuntimeStore.SOURCE_CANONICAL -> {
                        val canonical = requireNotNull(database.playerDao().findById(runtime.playerId)) {
                            "Missing canonical player ${runtime.playerId}"
                        }
                        StaticInput(
                            name = canonical.name,
                            positionCode = canonical.position,
                            sideCode = canonical.side,
                            cr1 = canonical.cr1,
                            cr2 = canonical.cr2,
                        )
                    }

                    CareerPlayerRuntimeStore.SOURCE_PROCEDURAL -> {
                        val procedural = requireNotNull(
                            playerRuntimeDao.findProceduralPlayer(careerId, runtime.playerId)
                        ) {
                            "Missing procedural player career=$careerId player=${runtime.playerId}"
                        }
                        StaticInput(
                            name = procedural.name,
                            positionCode = procedural.position,
                            sideCode = procedural.side,
                            cr1 = procedural.cr1,
                            cr2 = procedural.cr2,
                        )
                    }

                    else -> error("Unknown player runtime sourceType=${runtime.sourceType}")
                }

                PlayerInput(
                    playerId = runtime.playerId,
                    name = staticInput.name,
                    positionCode = staticInput.positionCode,
                    sideCode = staticInput.sideCode,
                    subroleCode = LegacyPlayerSubroleCodeRule.resolve(
                        positionCode = staticInput.positionCode,
                        cr1 = staticInput.cr1,
                        cr2 = staticInput.cr2,
                    ),
                    // CareerMatchPersistedRuntimeResolver hydrates the certified
                    // legacy match runtime from the career-specific overall value.
                    skill = runtime.overall,
                    energy = runtime.energy,
                    star = runtime.star,
                    sourceOrdinal = membership.sourceOrdinal,
                    blockedByM0ForPreparedMatch = preparedMatchEpochDay?.let { currentEpochDay ->
                        blockedByLegacyM0(
                            injuryUntilEpochDay = runtime.injuryUntilEpochDay,
                            currentEpochDay = currentEpochDay,
                        )
                    },
                )
            }

            LineupInputs(
                careerId = careerId,
                clubId = clubId,
                players = players,
                matchPreparation = matchPreparation,
            )
        }

    private suspend fun buildMatchPreparation(
        state: CareerState,
        managedClubId: String,
    ): MatchPreparation {
        val schedule = database.careerScheduledMatchDao().findAll(state.id).map { entity ->
            ScheduledCareerMatch(
                matchId = entity.matchId,
                dayIndex = entity.dayIndex,
                eventTypeCode = entity.eventTypeCode,
                homeClubId = entity.homeClubId,
                awayClubId = entity.awayClubId,
                processed = entity.processed,
            )
        }
        val nextSelection = LegacyCalendarRules.selectNextPlayableDay(
            state = state,
            scheduledDays = CareerScheduleCalendarProjection.calendarDays(schedule),
        )
        if (!nextSelection.found) {
            return unavailablePreparation(
                nextPlayableDayIndex = null,
                blocker = MatchPreparationBlocker.NO_PLAYABLE_MATCH,
            )
        }

        val nextDayIndex = requireNotNull(nextSelection.selectedIndex)
        val managedMatches = schedule.filter { match ->
            !match.processed &&
                match.dayIndex == nextDayIndex &&
                (match.homeClubId == managedClubId || match.awayClubId == managedClubId)
        }
        require(managedMatches.size <= 1) {
            "Managed club $managedClubId has multiple matches on next playable day $nextDayIndex"
        }
        val target = managedMatches.singleOrNull()
            ?: return unavailablePreparation(
                nextPlayableDayIndex = nextDayIndex,
                blocker = MatchPreparationBlocker.MANAGED_CLUB_NOT_ON_NEXT_PLAYABLE_DAY,
            )

        val playerRuntimeDao = database.careerPlayerRuntimeDao()
        suspend fun seniorRosterCount(clubId: String): Int {
            val seniorMemberships = playerRuntimeDao.membershipsForClub(
                careerId = state.id,
                clubId = clubId,
                rosterKind = ROSTER_SENIOR,
            )
            seniorMemberships.forEach { membership ->
                requireNotNull(playerRuntimeDao.findRuntime(state.id, membership.playerId)) {
                    "Missing runtime for career=${state.id} player=${membership.playerId}"
                }
            }
            return seniorMemberships.size
        }

        val homeSeniorRosterCount = seniorRosterCount(target.homeClubId)
        val awaySeniorRosterCount = seniorRosterCount(target.awayClubId)
        val managerDao = database.careerManagerRuntimeDao()
        val homeClubRuntime = managerDao.findClubRuntime(state.id, target.homeClubId)
        val awayClubRuntime = managerDao.findClubRuntime(state.id, target.awayClubId)
        val transientOwners = resolveTransientClubOwners(
            homeLegacyModeFlag = homeClubRuntime?.active,
            awayLegacyModeFlag = awayClubRuntime?.active,
        )
        val homeTacticIndex = homeClubRuntime?.let { runtime ->
            LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(
                CareerClubTacticsStore.toRawState(runtime)
            )
        }
        val awayTacticIndex = awayClubRuntime?.let { runtime ->
            LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(
                CareerClubTacticsStore.toRawState(runtime)
            )
        }

        val blockers = linkedSetOf<MatchPreparationBlocker>()
        if (homeSeniorRosterCount == 0) blockers += MatchPreparationBlocker.HOME_SENIOR_ROSTER_EMPTY
        if (awaySeniorRosterCount == 0) blockers += MatchPreparationBlocker.AWAY_SENIOR_ROSTER_EMPTY

        blockers += MatchPreparationBlocker.LINEUP_ELIGIBILITY_OWNER_UNRESOLVED
        if (homeTacticIndex == null || awayTacticIndex == null) {
            blockers += MatchPreparationBlocker.TACTICS_STATE_OWNER_UNRESOLVED
        }
        if (transientOwners == null) {
            // `best.s.N` is globally proven as {5,5}, but the match-side transient pack remains
            // fail-closed until both persisted `best.c0.Q0()` values are available. We therefore
            // do not publish a partial club-runtime input pack.
            blockers += MatchPreparationBlocker.SUBSTITUTION_BUDGET_OWNER_UNRESOLVED
            blockers += MatchPreparationBlocker.LEGACY_MODE_FLAG_OWNER_UNRESOLVED
        }
        blockers += MatchPreparationBlocker.MATCH_RUNTIME_COMPOSITION_UNWIRED

        return MatchPreparation(
            nextPlayableDayIndex = nextDayIndex,
            matchId = target.matchId,
            homeClubId = target.homeClubId,
            awayClubId = target.awayClubId,
            managedSide = if (target.homeClubId == managedClubId) {
                ManagedMatchSide.HOME
            } else {
                ManagedMatchSide.AWAY
            },
            homeSeniorRosterCount = homeSeniorRosterCount,
            awaySeniorRosterCount = awaySeniorRosterCount,
            homeTacticIndex = homeTacticIndex,
            awayTacticIndex = awayTacticIndex,
            homeSubstitutionsRemaining = transientOwners?.homeSubstitutionsRemaining,
            awaySubstitutionsRemaining = transientOwners?.awaySubstitutionsRemaining,
            homeLegacyModeFlag = transientOwners?.homeLegacyModeFlag,
            awayLegacyModeFlag = transientOwners?.awayLegacyModeFlag,
            blockers = blockers,
        )
    }

    private fun unavailablePreparation(
        nextPlayableDayIndex: Int?,
        blocker: MatchPreparationBlocker,
    ) = MatchPreparation(
        nextPlayableDayIndex = nextPlayableDayIndex,
        matchId = null,
        homeClubId = null,
        awayClubId = null,
        managedSide = null,
        homeSeniorRosterCount = null,
        awaySeniorRosterCount = null,
        homeTacticIndex = null,
        awayTacticIndex = null,
        homeSubstitutionsRemaining = null,
        awaySubstitutionsRemaining = null,
        homeLegacyModeFlag = null,
        awayLegacyModeFlag = null,
        blockers = setOf(blocker),
    )

    private data class StaticInput(
        val name: String,
        val positionCode: Int,
        val sideCode: Int,
        val cr1: Int,
        val cr2: Int,
    )

    companion object {
        private const val ROSTER_SENIOR = "SENIOR"

        /**
         * Exact day-granularity projection of `best.o.M0()` for the already-certified modern
         * `injuryUntilEpochDay` owner. Legacy M0 is strict `J > 0 && J > currentCalendarMillis`.
         */
        internal fun blockedByLegacyM0(
            injuryUntilEpochDay: Long,
            currentEpochDay: Long,
        ): Boolean {
            require(injuryUntilEpochDay >= 0L) { "Injury deadline must not be negative" }
            return injuryUntilEpochDay > 0L && injuryUntilEpochDay > currentEpochDay
        }

        /**
         * Joins the two recovered transient owners used by `best.s`: constructor-owned `N={5,5}`
         * and persisted club `Q0()`. Missing Q0 evidence keeps the whole pack fail-closed.
         */
        internal fun resolveTransientClubOwners(
            homeLegacyModeFlag: Boolean?,
            awayLegacyModeFlag: Boolean?,
        ): TransientClubOwners? {
            if (homeLegacyModeFlag == null || awayLegacyModeFlag == null) return null
            return TransientClubOwners(
                homeSubstitutionsRemaining = LegacyMatchSubstitutionRules.INITIAL_SUBSTITUTIONS_PER_SIDE,
                awaySubstitutionsRemaining = LegacyMatchSubstitutionRules.INITIAL_SUBSTITUTIONS_PER_SIDE,
                homeLegacyModeFlag = homeLegacyModeFlag,
                awayLegacyModeFlag = awayLegacyModeFlag,
            )
        }
    }
}
