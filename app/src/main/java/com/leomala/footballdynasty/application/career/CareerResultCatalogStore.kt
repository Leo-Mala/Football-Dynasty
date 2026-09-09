package com.leomala.footballdynasty.application.career

/**
 * Read-only Phase 17 owner for persisted match results.
 *
 * Results are derived exclusively from the persisted calendar owner. Pending matches are excluded;
 * a processed match without a complete persisted score is treated as corrupted state and fails
 * closed instead of being presented as a fabricated or partial result.
 */
class CareerResultCatalogStore(
    private val calendarCatalogStore: CareerCalendarCatalogStore,
) {
    data class ResultCatalog(
        val careerId: String,
        val currentDayIndex: Int,
        val rows: List<ResultRow>,
    )

    data class ResultRow(
        val matchId: String,
        val dayIndex: Int,
        val eventTypeCode: Int,
        val homeClubId: String,
        val homeClubName: String,
        val awayClubId: String,
        val awayClubName: String,
        val homeGoals: Int,
        val awayGoals: Int,
        val competitionLinks: List<CareerCalendarCatalogStore.CompetitionLink>,
    )

    suspend fun loadResults(careerId: String): ResultCatalog? {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        val calendar = calendarCatalogStore.loadCalendar(careerId) ?: return null
        val rows = calendar.matches
            .filter { it.processed }
            .map { match ->
                ResultRow(
                    matchId = match.matchId,
                    dayIndex = match.dayIndex,
                    eventTypeCode = match.eventTypeCode,
                    homeClubId = match.homeClubId,
                    homeClubName = match.homeClubName,
                    awayClubId = match.awayClubId,
                    awayClubName = match.awayClubName,
                    homeGoals = checkNotNull(match.homeGoals) {
                        "Processed match ${match.matchId} is missing persisted home goals"
                    },
                    awayGoals = checkNotNull(match.awayGoals) {
                        "Processed match ${match.matchId} is missing persisted away goals"
                    },
                    competitionLinks = match.competitionLinks,
                )
            }

        return ResultCatalog(
            careerId = calendar.careerId,
            currentDayIndex = calendar.currentDayIndex,
            rows = rows,
        )
    }
}
