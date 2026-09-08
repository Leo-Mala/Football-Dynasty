package com.leomala.footballdynasty.ui.navigation

/**
 * Presentation destinations explicitly authorized by the Phase 17 roadmap and
 * the Phase 15 parity matrix.
 *
 * This type intentionally carries no gameplay behavior. Each destination may
 * be exposed by production navigation only after its UI action is wired to an
 * already-certified modern owner. Keeping the list closed prevents navigation
 * work from silently inventing new game functions.
 */
enum class LegacyUiDestination {
    START,
    NEW_OR_LOAD_CAREER,
    CLUB_SELECTION,
    CAREER_HOME,
    SQUAD,
    LINEUP_AND_TACTICS,
    CALENDAR_AND_ROUNDS,
    STANDINGS,
    LIVE_MATCH,
    RESULTS,
    TRANSFER_MARKET,
    CONTRACTS,
    FINANCES,
    STADIUM,
    MANAGER,
    JUNIORS,
    SEASON_END,
}
