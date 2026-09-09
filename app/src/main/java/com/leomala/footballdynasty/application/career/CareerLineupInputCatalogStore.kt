package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.domain.career.CareerScheduleCalendarProjection
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyPlayerSubroleCodeRule

/**
 * Read-only Phase 17 boundary for the persisted inputs already proven to feed
 * the legacy lineup runtime.
 *
 * The boundary also exposes fail-closed readiness for the next managed match.
 * It never substitutes test fixtures/defaults for unresolved legacy owners.
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
        val blockers: Set<MatchPreparationBlocker>,
    ) {
        val executable: Boolean
            get() = matchId != null && blockers.isEmpty()
    }

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
                )
            }

            LineupInputs(
                careerId = careerId,
                clubId = clubId,
                players = players,
                matchPreparation = buildMatchPreparation(
                    state = state,
                    managedClubId = clubId,
                ),
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
            return MatchPreparation(
                nextPlayableDayIndex = null,
                matchId = null,
                homeClubId = null,
                awayClubId = null,
                managedSide = null,
                homeSeniorRosterCount = null,
                awaySeniorRosterCount = null,
                blockers = setOf(MatchPreparationBlocker.NO_PLAYABLE_MATCH),
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
            ?: return MatchPreparation(
                nextPlayableDayIndex = nextDayIndex,
                matchId = null,
                homeClubId = null,
                awayClubId = null,
                managedSide = null,
                homeSeniorRosterCount = null,
                awaySeniorRosterCount = null,
                blockers = setOf(MatchPreparationBlocker.MANAGED_CLUB_NOT_ON_NEXT_PLAYABLE_DAY),
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
        val blockers = linkedSetOf<MatchPreparationBlocker>()
        if (homeSeniorRosterCount == 0) blockers += MatchPreparationBlocker.HOME_SENIOR_ROSTER_EMPTY
        if (awaySeniorRosterCount == 0) blockers += MatchPreparationBlocker.AWAY_SENIOR_ROSTER_EMPTY

        // These values are deliberately not inferred from current tests or modern defaults. They
        // remain blocked until their legacy producers/owners are connected to persisted production state.
        blockers += MatchPreparationBlocker.LINEUP_ELIGIBILITY_OWNER_UNRESOLVED
        blockers += MatchPreparationBlocker.TACTICS_STATE_OWNER_UNRESOLVED
        blockers += MatchPreparationBlocker.SUBSTITUTION_BUDGET_OWNER_UNRESOLVED
        blockers += MatchPreparationBlocker.LEGACY_MODE_FLAG_OWNER_UNRESOLVED
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
            blockers = blockers,
        )
    }

    private data class StaticInput(
        val name: String,
        val positionCode: Int,
        val sideCode: Int,
        val cr1: Int,
        val cr2: Int,
    )

    private companion object {
        const val ROSTER_SENIOR = "SENIOR"
    }
}
