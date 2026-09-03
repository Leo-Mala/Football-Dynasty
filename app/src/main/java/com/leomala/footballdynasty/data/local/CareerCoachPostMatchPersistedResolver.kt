package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyCoachAssociatedClub
import com.leomala.footballdynasty.domain.manager.LegacyCoachMatchClubManagerRef
import com.leomala.footballdynasty.domain.manager.LegacyCoachMatchManagerResolutionRule
import com.leomala.footballdynasty.domain.manager.LegacyCoachPostMatchAdjustmentRule
import com.leomala.footballdynasty.domain.manager.LegacyManagerIdentityRef

/**
 * Persisted production adapter for reachable `best.s.f() -> f0.j()/i()` coach post-match paths.
 *
 * Type 7 is fully executable because legacy excludes `f0.i()` for that type and every `f0.j()`
 * input is already persisted losslessly. Types 1,2,3,4,5,6,8 are deliberately fail-closed when a
 * persisted manager is attached to either match club: their raw `konrent.t.x0()` / c0.I(k0)
 * inputs are not yet losslessly persisted, so silently dropping the coach mutation would be a
 * false-success implementation. Matches with no attached persisted manager remain exact no-ops.
 */
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
    ): List<CareerMatchCoachUpdate> = resolveTypeSeven(
        careerId = careerId,
        scheduled = scheduled,
        seasonId = seasonId,
        homeGoals = homeGoals,
        awayGoals = awayGoals,
    )

    suspend fun resolveTypeSeven(
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
        if (competition.legacyCompetitionType != 7) {
            if (competition.legacyCompetitionType !in LegacyCoachPostMatchAdjustmentRule.callerCompetitionTypes) {
                return emptyList()
            }
            val homeManagerId = ticketStore.findClubState(careerId, scheduled.homeClubId)?.legacyManagerId
            val awayManagerId = ticketStore.findClubState(careerId, scheduled.awayClubId)?.legacyManagerId
            val hasAttachedManager = sequenceOf(homeManagerId, awayManagerId).any { it != null && it != -1 }
            if (!hasAttachedManager) return emptyList()
            throw IllegalStateException(
                "Coach post-match type ${competition.legacyCompetitionType} requires raw legacy " +
                    "konrent.t.x0() and LoadLigaOptions.nRebaixados inputs before f0.j()/i() can execute"
            )
        }

        val homeClub = exactLegacyClub(scheduled.homeClubId)
        val awayClub = exactLegacyClub(scheduled.awayClubId)
        val homeTicket = requireNotNull(ticketStore.findClubState(careerId, scheduled.homeClubId)) {
            "Missing materialized home club manager state $careerId/${scheduled.homeClubId}"
        }
        val awayTicket = requireNotNull(ticketStore.findClubState(careerId, scheduled.awayClubId)) {
            "Missing materialized away club manager state $careerId/${scheduled.awayClubId}"
        }

        val managerIdentities = ticketStore.managersInWorldOrder(careerId).map {
            LegacyManagerIdentityRef(it.sourceOrdinal, it.legacyManagerId)
        }
        val resolvedManagers = LegacyCoachMatchManagerResolutionRule.orderedForMatch(
            home = LegacyCoachMatchClubManagerRef(homeClub.clubId, homeTicket.legacyManagerId),
            away = LegacyCoachMatchClubManagerRef(awayClub.clubId, awayTicket.legacyManagerId),
            managersInWorldOrder = managerIdentities,
        )

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

        val associatedClubIds = linkedSetOf(homeClub.clubId, awayClub.clubId)
        coachStates.values.forEach { state ->
            state.currentClubId?.let(associatedClubIds::add)
            state.alternativeClubId?.let(associatedClubIds::add)
        }
        val associatedClubs = linkedMapOf<String, LegacyCoachAssociatedClub>()
        associatedClubIds.forEach { clubId ->
            associatedClubs[clubId] = when (clubId) {
                homeClub.clubId -> homeClub
                awayClub.clubId -> awayClub
                else -> exactLegacyClub(clubId)
            }
        }

        return CareerCoachPostMatchUpdateResolver.resolve(
            managersInWorldOrder = managerIdentities,
            coachStateBySourceOrdinal = coachStates,
            evidence = CareerCoachPostMatchLegacyEvidence(
                seasonId = seasonId,
                rawCompetitionType = 7,
                leagueCompetitionSubtype = null,
                homeClub = homeClub,
                awayClub = awayClub,
                homeStoredManagerId = homeTicket.legacyManagerId,
                awayStoredManagerId = awayTicket.legacyManagerId,
                homeGoals = homeGoals,
                awayGoals = awayGoals,
                associatedClubsById = associatedClubs,
                adjustment = null,
            ),
        )
    }

    private suspend fun exactLegacyClub(clubId: String): LegacyCoachAssociatedClub {
        val club = requireNotNull(database.clubDao().findById(clubId)) {
            "Missing immutable club $clubId for coach post-match resolution"
        }
        require(club.legacyValid) {
            "Club $clubId has no validated legacy identity for coach post-match resolution"
        }
        return LegacyCoachAssociatedClub(clubId = club.id, legacyClubId = club.legacyId)
    }
}
