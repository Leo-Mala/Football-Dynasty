package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.domain.career.CareerState

/**
 * UI-session owner for the Phase 17 career-entry flow.
 *
 * This keeps the currently opened career separate from the persisted entry catalog. Closing a
 * career always crosses [CareerEntryFlowCoordinator.closeCareerAndRefreshEntry], so Compose can
 * return to a freshly read save list instead of retaining an in-memory career snapshot.
 */
class CareerEntrySessionController(
    private val coordinator: CareerEntryFlowCoordinator,
) {
    data class State(
        val entry: CareerEntryUiState? = null,
        val loadedCareer: CareerState? = null,
        val loadError: Boolean = false,
    )

    suspend fun openEntry(): State = State(entry = coordinator.openEntry())

    suspend fun openCareer(
        current: State,
        careerId: String,
    ): State = when (val result = coordinator.openCareer(careerId)) {
        is CareerEntryOpenResult.Loaded -> current.copy(
            loadedCareer = result.state,
            loadError = false,
        )
        is CareerEntryOpenResult.NotLoadable -> current.copy(
            loadedCareer = null,
            loadError = true,
        )
    }

    fun careerChanged(
        current: State,
        career: CareerState,
    ): State = current.copy(
        loadedCareer = career,
        loadError = false,
    )

    suspend fun closeCareer(current: State): State = current.copy(
        entry = coordinator.closeCareerAndRefreshEntry(),
        loadedCareer = null,
        loadError = false,
    )
}
