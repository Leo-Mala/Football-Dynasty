package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerEntryCatalogStore
import com.leomala.footballdynasty.application.career.CareerEntryCommandStore
import com.leomala.footballdynasty.application.career.CareerEntrySummary
import com.leomala.footballdynasty.application.career.CareerSelectableClub
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.ui.navigation.LegacyUiDestination

/**
 * Presentation coordinator for the proven Phase 17 entry flow.
 *
 * Automatic career identity/RNG-seed policy is deliberately not synthesized here. New-career
 * creation is allowed only when the product UI supplies both values explicitly and the club came
 * from the canonical persisted selection list.
 */
class CareerEntryFlowCoordinator(
    private val listCareers: suspend () -> List<CareerEntrySummary>,
    private val loadCareer: suspend (String) -> CareerState?,
    private val listClubs: suspend () -> List<CareerSelectableClub>,
    private val createCareer: (suspend (String, String?, Long, String) -> CareerState)? = null,
) {
    constructor(
        catalogStore: CareerEntryCatalogStore,
        commandStore: CareerEntryCommandStore? = null,
    ) : this(
        listCareers = catalogStore::listCareers,
        loadCareer = catalogStore::loadCareer,
        listClubs = catalogStore::selectableClubs,
        createCareer = commandStore?.let { store ->
            { careerId, displayName, seed, managedClubId ->
                store.createCareer(
                    careerId = careerId,
                    displayName = displayName,
                    seed = seed,
                    managedClubId = managedClubId,
                )
            }
        },
    )

    val careerCreationAvailable: Boolean
        get() = createCareer != null

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

    suspend fun createCareerFromExplicitInputs(
        state: CareerEntryUiState,
        careerId: String,
        displayName: String?,
        seed: Long,
    ): CareerState {
        require(state.destination == LegacyUiDestination.CLUB_SELECTION) {
            "Career creation requires the canonical club-selection destination"
        }
        val clubId = requireNotNull(state.selectedClubId) {
            "Career creation requires an explicitly selected canonical club"
        }
        require(state.clubs.any { it.id == clubId }) {
            "Selected club must come from the canonical selectable-club list"
        }
        val create = requireNotNull(createCareer) {
            "Career creation command boundary is not wired"
        }
        return create(careerId, displayName, seed, clubId)
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
