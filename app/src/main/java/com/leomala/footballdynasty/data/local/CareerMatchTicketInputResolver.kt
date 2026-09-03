package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyTicketCalculationInput
import com.leomala.footballdynasty.domain.manager.LegacyTicketClubSourceFields
import com.leomala.footballdynasty.domain.manager.LegacyTicketClubSourceRule
import com.leomala.footballdynasty.domain.manager.LegacyTicketParentClassRule

/**
 * Resolves every characterized ticket input from persisted/source state.
 *
 * V9 closes the former caller-owned O/coach-H/parent-class seam. Missing V9 materialization is an
 * error rather than an invitation to substitute immutable club level, constant H=80 or a guessed
 * competition-type mapping.
 */
class CareerMatchTicketInputResolver(
    private val database: FootballDynastyDatabase,
) {
    private val ticketStore = CareerTicketRuntimeStore(database)

    suspend fun resolve(
        careerId: String,
        scheduled: ScheduledCareerMatch,
    ): CareerMatchTicketRuntimeInput {
        val competitionDao = database.careerCompetitionDao()
        val links = competitionDao.matchLinksForMatch(careerId, scheduled.matchId)
        require(links.size == 1) {
            "Ticket-bearing match ${scheduled.matchId} must resolve exactly one persisted competition link"
        }
        val competition = requireNotNull(
            competitionDao.findCompetition(careerId, links.single().competitionId)
        ) {
            "Missing persisted competition for match ${scheduled.matchId}"
        }

        val clubDao = database.clubDao()
        val home = requireNotNull(clubDao.findById(scheduled.homeClubId)) {
            "Missing immutable home club ${scheduled.homeClubId}"
        }
        val away = requireNotNull(clubDao.findById(scheduled.awayClubId)) {
            "Missing immutable away club ${scheduled.awayClubId}"
        }
        val source = LegacyTicketClubSourceRule.project(
            home = LegacyTicketClubSourceFields(home.country, home.reputation),
            away = LegacyTicketClubSourceFields(away.country, away.reputation),
        )

        val homeRuntime = requireNotNull(
            database.careerManagerRuntimeDao().findClubRuntime(careerId, scheduled.homeClubId)
        ) {
            "Missing materialized home manager runtime $careerId/${scheduled.homeClubId}"
        }
        val homeTicketState = requireNotNull(
            ticketStore.findClubState(careerId, scheduled.homeClubId)
        ) {
            "Missing materialized home ticket runtime $careerId/${scheduled.homeClubId}"
        }
        val constructionSource = requireNotNull(
            ticketStore.findMatchConstructionSource(careerId, scheduled.matchId)
        ) {
            "Missing materialized match construction source $careerId/${scheduled.matchId}"
        }
        val coachH = ticketStore.resolveCoachRawH(careerId, homeTicketState.legacyManagerId)

        return CareerMatchTicketRuntimeInput(
            calculation = LegacyTicketCalculationInput(
                capacities = emptyList(),
                rawCompetitionType = competition.legacyCompetitionType,
                homeRawO = homeTicketState.rawDivisionCode,
                homeRawP0 = source.homeRawP0,
                awayRawP0 = source.awayRawP0,
                homeRawJ = source.homeRawJ,
                homeRegionalPercent = coachH,
                rawCompetitionAIsKonrentA0 =
                    LegacyTicketParentClassRule.parentCompetitionIsA0(constructionSource),
            ),
            homeLegacyQ0 = homeRuntime.active,
        )
    }
}
