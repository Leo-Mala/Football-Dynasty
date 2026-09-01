package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyTicketCalculationInput
import com.leomala.footballdynasty.domain.manager.LegacyTicketClubSourceFields
import com.leomala.footballdynasty.domain.manager.LegacyTicketClubSourceRule

/**
 * Ticket inputs that are proven career-mutable but are not yet represented by the current V8
 * persistence model.
 *
 * Callers may supply these values only from an already-characterized legacy/runtime boundary:
 * - [homeRawO] is `best.c0.O()` (league/division code written by `konrent.t.f1()`);
 * - [homeLegacyCoachHOrNull] is `best.c0.y0()?.o()` (`best.f0.H`), null only when y0() is null;
 * - [parentCompetitionIsA0] is the exact `best.s.A() instanceof konrent.a0` result.
 *
 * Competition type, p0, J and Q0 are intentionally absent: they are resolved from persisted state.
 */
data class CareerMatchTicketUnpersistedEvidence(
    val homeRawO: Int,
    val homeLegacyCoachHOrNull: Int?,
    val parentCompetitionIsA0: Boolean,
)

/**
 * Resolves the ticket fields already proven to exist in V8/source state, reducing the amount of raw
 * state a match caller can accidentally fabricate.
 */
class CareerMatchTicketInputResolver(
    private val database: FootballDynastyDatabase,
) {
    suspend fun resolve(
        careerId: String,
        scheduled: ScheduledCareerMatch,
        evidence: CareerMatchTicketUnpersistedEvidence,
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

        return CareerMatchTicketRuntimeInput(
            calculation = LegacyTicketCalculationInput(
                // Replaced by the durable V8 four-sector vector immediately before calculation.
                capacities = emptyList(),
                rawCompetitionType = competition.legacyCompetitionType,
                homeRawO = evidence.homeRawO,
                homeRawP0 = source.homeRawP0,
                awayRawP0 = source.awayRawP0,
                homeRawJ = source.homeRawJ,
                homeRegionalPercent = evidence.homeLegacyCoachHOrNull,
                rawCompetitionAIsKonrentA0 = evidence.parentCompetitionIsA0,
            ),
            homeLegacyQ0 = homeRuntime.active,
        )
    }
}
