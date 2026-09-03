package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyCoachAdjustmentSide
import com.leomala.footballdynasty.domain.manager.LegacyCoachAssociatedClub
import com.leomala.footballdynasty.domain.manager.LegacyCoachLeagueStandingInput
import com.leomala.footballdynasty.domain.manager.LegacyCoachMatchClubManagerRef
import com.leomala.footballdynasty.domain.manager.LegacyCoachMatchManagerResolutionRule
import com.leomala.footballdynasty.domain.manager.LegacyCoachPostMatchAdjustmentRule
import com.leomala.footballdynasty.domain.manager.LegacyManagerIdentityRef

/** Persisted production adapter for reachable `best.s.f() -> f0.j()/i()` coach post-match paths. */
class CareerCoachPostMatchPersistedResolver(
    private val database: FootballDynastyDatabase,
) {
    private val ticketStore = CareerTicketRuntimeStore(database)
    private val coachStore = CareerCoachRuntimeStore(database)

    suspend fun resolveReachable(
        careerId: String,
        scheduled: ScheduledCareerMatch,
        seasonId: Int,
        homeGoals: Int,
        awayGoals: Int,
    ): List<CareerMatchCoachUpdate> {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        require(seasonId >= 0) { "Legacy season id must not be negative" }
        require(homeGoals >= 0 && awayGoals >= 0) { "Resolved goals must not be negative" }

        val competitionDao = database.careerCompetitionDao()
        val links = competitionDao.matchLinksForMatch(careerId, scheduled.matchId)
        if (links.isEmpty()) return emptyList()
        require(links.size == 1) {
            "Scheduled match ${scheduled.matchId} belongs to multiple persisted competitions"
        }
        val competition = requireNotNull(
            competitionDao.findCompetition(careerId, links.single().competitionId)
        ) { "Missing persisted competition ${links.single().competitionId} for match ${scheduled.matchId}" }

        val home = exactLegacyClub(scheduled.homeClubId)
        val away = exactLegacyClub(scheduled.awayClubId)
        val homeTicket = requireNotNull(ticketStore.findClubState(careerId, scheduled.homeClubId)) {
            "Missing materialized home club manager state $careerId/${scheduled.homeClubId}"
        }
        val awayTicket = requireNotNull(ticketStore.findClubState(careerId, scheduled.awayClubId)) {
            "Missing materialized away club manager state $careerId/${scheduled.awayClubId}"
        }
        if (homeTicket.legacyManagerId == -1 && awayTicket.legacyManagerId == -1) return emptyList()

        val managerIdentities = ticketStore.managersInWorldOrder(careerId).map {
            LegacyManagerIdentityRef(it.sourceOrdinal, it.legacyManagerId)
        }
        val resolvedManagers = LegacyCoachMatchManagerResolutionRule.orderedForMatch(
            home = LegacyCoachMatchClubManagerRef(home.associated.clubId, homeTicket.legacyManagerId),
            away = LegacyCoachMatchClubManagerRef(away.associated.clubId, awayTicket.legacyManagerId),
            managersInWorldOrder = managerIdentities,
        )
        if (resolvedManagers.isEmpty()) return emptyList()

        val coachStates = linkedMapOf<Int, CareerCoachRuntimeState>()
        resolvedManagers.forEach { resolved ->
            val ordinal = resolved.manager.sourceOrdinal
            val state = requireNotNull(coachStore.find(careerId, ordinal)) {
                "Resolved manager ${resolved.manager.legacyManagerId} at $ordinal has no materialized V11 coach state"
            }
            require(state.legacyManagerId == resolved.manager.legacyManagerId) {
                "Resolved manager V9/V11 identity diverged at source ordinal $ordinal"
            }
            coachStates.putIfAbsent(ordinal, state)
        }

        val subtype = if (competition.legacyCompetitionType == 1 || competition.legacyCompetitionType == 3) {
            requireNotNull(competition.legacyLeagueSubtype) {
                "Coach post-match type ${competition.legacyCompetitionType} requires exact legacy konrent.t.x0()"
            }
        } else {
            competition.legacyLeagueSubtype
        }

        val standingByClubId = resolveStandingInputs(
            careerId = careerId,
            competitionId = competition.competitionId,
            rawCompetitionType = competition.legacyCompetitionType,
            relegationCount = competition.legacyRelegationCount,
            coachStates = coachStates.values,
            homeClubId = home.associated.clubId,
            awayClubId = away.associated.clubId,
        )
        val cashByClubId = resolveRequiredCash(
            careerId = careerId,
            rawCompetitionType = competition.legacyCompetitionType,
            coachStates = coachStates.values,
            standingByClubId = standingByClubId,
            homeClubId = home.associated.clubId,
            awayClubId = away.associated.clubId,
        )

        val associatedClubIds = linkedSetOf(home.associated.clubId, away.associated.clubId)
        coachStates.values.forEach { state ->
            state.currentClubId?.let(associatedClubIds::add)
            state.alternativeClubId?.let(associatedClubIds::add)
        }
        val associatedClubs = linkedMapOf<String, LegacyCoachAssociatedClub>()
        associatedClubIds.forEach { clubId ->
            associatedClubs[clubId] = when (clubId) {
                home.associated.clubId -> home.associated
                away.associated.clubId -> away.associated
                else -> exactLegacyClub(clubId).associated
            }
        }

        val adjustment = if (
            competition.legacyCompetitionType in LegacyCoachPostMatchAdjustmentRule.callerCompetitionTypes
        ) {
            CareerCoachPostMatchAdjustmentEvidence(
                homeStrength = home.rawStrength,
                awayStrength = away.rawStrength,
                isLegacyLeagueCompetition = true,
                standingByClubId = standingByClubId,
                cashByClubId = cashByClubId,
            )
        } else {
            null
        }

        return CareerCoachPostMatchUpdateResolver.resolve(
            managersInWorldOrder = managerIdentities,
            coachStateBySourceOrdinal = coachStates,
            evidence = CareerCoachPostMatchLegacyEvidence(
                seasonId = seasonId,
                rawCompetitionType = competition.legacyCompetitionType,
                leagueCompetitionSubtype = subtype,
                homeClub = home.associated,
                awayClub = away.associated,
                homeStoredManagerId = homeTicket.legacyManagerId,
                awayStoredManagerId = awayTicket.legacyManagerId,
                homeGoals = homeGoals,
                awayGoals = awayGoals,
                associatedClubsById = associatedClubs,
                adjustment = adjustment,
            ),
        )
    }

    suspend fun resolveTypeSeven(
        careerId: String,
        scheduled: ScheduledCareerMatch,
        seasonId: Int,
        homeGoals: Int,
        awayGoals: Int,
    ): List<CareerMatchCoachUpdate> = resolveReachable(
        careerId = careerId,
        scheduled = scheduled,
        seasonId = seasonId,
        homeGoals = homeGoals,
        awayGoals = awayGoals,
    )

    private suspend fun resolveStandingInputs(
        careerId: String,
        competitionId: String,
        rawCompetitionType: Int,
        relegationCount: Int?,
        coachStates: Collection<CareerCoachRuntimeState>,
        homeClubId: String,
        awayClubId: String,
    ): Map<String, LegacyCoachLeagueStandingInput?> {
        if (rawCompetitionType != 1 && rawCompetitionType != 3) return emptyMap()
        val needsStanding = coachStates.any { it.currentClubId == homeClubId || it.currentClubId == awayClubId }
        if (!needsStanding) return emptyMap()
        val exactRelegationCount = requireNotNull(relegationCount) {
            "Coach post-match type $rawCompetitionType requires exact legacy LoadLigaOptions.nRebaixados"
        }
        val rows = database.careerCompetitionDao().standings(careerId, competitionId)
        val tableSize = rows.size
        return linkedMapOf(
            homeClubId to rows.indexOfFirst { it.clubId == homeClubId }.takeIf { it >= 0 }?.let {
                LegacyCoachLeagueStandingInput(it + 1, tableSize, exactRelegationCount)
            },
            awayClubId to rows.indexOfFirst { it.clubId == awayClubId }.takeIf { it >= 0 }?.let {
                LegacyCoachLeagueStandingInput(it + 1, tableSize, exactRelegationCount)
            },
        )
    }

    private suspend fun resolveRequiredCash(
        careerId: String,
        rawCompetitionType: Int,
        coachStates: Collection<CareerCoachRuntimeState>,
        standingByClubId: Map<String, LegacyCoachLeagueStandingInput?>,
        homeClubId: String,
        awayClubId: String,
    ): Map<String, Long> {
        if (rawCompetitionType !in setOf(1, 3, 4, 6)) return emptyMap()
        val result = linkedMapOf<String, Long>()
        coachStates.filter { it.isUserControlled }.forEach { state ->
            val side = when (state.currentClubId) {
                homeClubId -> LegacyCoachAdjustmentSide.HOME
                awayClubId -> LegacyCoachAdjustmentSide.AWAY
                else -> LegacyCoachAdjustmentSide.NONE
            }
            val needsCash = when (side) {
                LegacyCoachAdjustmentSide.NONE -> true
                LegacyCoachAdjustmentSide.HOME,
                LegacyCoachAdjustmentSide.AWAY ->
                    rawCompetitionType in setOf(1, 3) && standingByClubId[state.currentClubId] != null
            }
            if (needsCash) {
                val clubId = state.currentClubId ?: return@forEach
                val runtime = requireNotNull(database.careerManagerRuntimeDao().findClubRuntime(careerId, clubId)) {
                    "Missing exact legacy club cash for user manager current club $careerId/$clubId"
                }
                result[clubId] = runtime.cash
            }
        }
        return result
    }

    private suspend fun exactLegacyClub(clubId: String): ExactLegacyClub {
        val club = requireNotNull(database.clubDao().findById(clubId)) {
            "Missing immutable club $clubId for coach post-match resolution"
        }
        require(club.legacyValid) {
            "Club $clubId has no validated legacy identity for coach post-match resolution"
        }
        return ExactLegacyClub(
            associated = LegacyCoachAssociatedClub(clubId = club.id, legacyClubId = club.legacyId),
            rawStrength = club.level,
        )
    }

    private data class ExactLegacyClub(
        val associated: LegacyCoachAssociatedClub,
        val rawStrength: Int,
    )
}
