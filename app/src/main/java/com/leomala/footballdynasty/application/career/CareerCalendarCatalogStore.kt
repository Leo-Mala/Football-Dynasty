package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/** Read-only Phase 17 projection of the persisted career calendar and competition round links. */
class CareerCalendarCatalogStore(
    private val database: FootballDynastyDatabase,
) {
    data class CalendarSnapshot(
        val careerId: String,
        val currentDayIndex: Int,
        val matches: List<MatchRow>,
    )

    data class MatchRow(
        val matchId: String,
        val dayIndex: Int,
        val eventTypeCode: Int,
        val homeClubId: String,
        val homeClubName: String,
        val awayClubId: String,
        val awayClubName: String,
        val processed: Boolean,
        val homeGoals: Int?,
        val awayGoals: Int?,
        val competitionLinks: List<CompetitionLink>,
    )

    data class CompetitionLink(
        val competitionId: String,
        val roundNumber: Int,
        val fixtureOrdinal: Int,
    )

    suspend fun loadCalendar(careerId: String): CalendarSnapshot? = database.withTransaction {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        val core = database.careerCoreStateDao().findById(careerId)
            ?: return@withTransaction null

        val clubDao = database.clubDao()
        val competitionDao = database.careerCompetitionDao()
        val matches = database.careerScheduledMatchDao().findAll(careerId).map { scheduled ->
            val home = requireNotNull(clubDao.findById(scheduled.homeClubId)) {
                "Missing home club ${scheduled.homeClubId} for career=$careerId match=${scheduled.matchId}"
            }
            val away = requireNotNull(clubDao.findById(scheduled.awayClubId)) {
                "Missing away club ${scheduled.awayClubId} for career=$careerId match=${scheduled.matchId}"
            }
            MatchRow(
                matchId = scheduled.matchId,
                dayIndex = scheduled.dayIndex,
                eventTypeCode = scheduled.eventTypeCode,
                homeClubId = scheduled.homeClubId,
                homeClubName = home.name,
                awayClubId = scheduled.awayClubId,
                awayClubName = away.name,
                processed = scheduled.processed,
                homeGoals = scheduled.homeGoals,
                awayGoals = scheduled.awayGoals,
                competitionLinks = competitionDao.matchLinksForMatch(careerId, scheduled.matchId).map { link ->
                    CompetitionLink(
                        competitionId = link.competitionId,
                        roundNumber = link.roundNumber,
                        fixtureOrdinal = link.fixtureOrdinal,
                    )
                },
            )
        }

        CalendarSnapshot(
            careerId = careerId,
            currentDayIndex = core.currentDayIndex,
            matches = matches,
        )
    }
}
