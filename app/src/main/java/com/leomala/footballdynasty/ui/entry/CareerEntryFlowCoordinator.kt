package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerEntryCatalogStore
import com.leomala.footballdynasty.application.career.CareerEntrySummary
import com.leomala.footballdynasty.application.career.CareerSelectableClub
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.ui.navigation.LegacyUiDestination

/**
 * Presentation coordinator for the first proven Phase 17 entry flow.
 *
 * This class deliberately owns no career identity or RNG seed policy. Those
 * values still have to come from a separately proven owner before the UI may
 * create a career. The coordinator only exposes persisted careers and canonical
 * clubs through the already-certified read boundary.
 */
class CareerEntryFlowCoordinator(
    private val listCareers: suspend () -> List<CareerEntrySummary>,
    private val loadCareer: suspend (String) -> CareerState?,
    private val listClubs: suspend () -> List<CareerSelectableClub>,
) {
    constructor(catalogStore: CareerEntryCatalogStore) : this(
        listCareers = catalogStore::listCareers,
        loadCareer = catalogStore::loadCareer,
        listClubs = catalogStore::selectableClubs,
    )

    suspend fun openEntry(): CareerEntryUiState =
        CareerEntryUiState(
            destination = LegacyUiDestination.NEW_OR_LOAD_CAREER,
            careers = listCareers(),
        )

    suspend fun openClubSelection(): CareerEntryUiState =
        CareerEntryUiState(
            destination = LegacyUiDestination.CLUB_SELECTION,
            clubs = listClubs(),
        )

    suspend fun openCareer(careerId: String): CareerEntryOpenResult {
        val state = loadCareer(careerId)
            ?: return CareerEntryOpenResult.NotLoadable(careerId)
        return CareerEntryOpenResult.Loaded(state)
    }

    fun selectClub(
        state: CareerEntryUiState,
        clubId: String,
    ): CareerEntryUiState {
        require(state.destination == LegacyUiDestination.CLUB_SELECTION) {
            "Club selection is only valid on the club-selection destination"
        }
        require(state.clubs.any { it.id == clubId }) {
            "Selected club must come from the canonical selectable-club list"
        }
        return state.copy(selectedClubId = clubId)
    }
}

data class CareerEntryUiState(
    val destination: LegacyUiDestination,
    val careers: List<CareerEntrySummary> = emptyList(),
    val clubs: List<CareerSelectableClub> = emptyList(),
    val selectedClubId: String? = null,
)

sealed interface CareerEntryOpenResult {
    data class Loaded(val state: CareerState) : CareerEntryOpenResult
    data class NotLoadable(val careerId: String) : CareerEntryOpenResult
}
