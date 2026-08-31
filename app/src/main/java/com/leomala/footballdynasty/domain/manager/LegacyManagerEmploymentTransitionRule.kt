package com.leomala.footballdynasty.domain.manager

/**
 * Career date copied into the manager-change history by legacy `best.f0.l(...)`.
 */
data class LegacyManagerEmploymentDate(
    val year: Int,
    val month: Int,
    val day: Int,
)

data class LegacyManagerEmploymentState(
    val managerId: String,
    val isUserControlled: Boolean,
    val currentClubId: String?,
    val previousClubId: String?,
    val previousClubCountry: Int?,
    val previousClubDivisionIndex: Int?,
    val rawG: Int,
    val rawH: Int,
    val rawM: Int,
)

data class LegacyManagerEmploymentClubState(
    val clubId: String,
    val legacyWorldId: Int,
    val countryCode: Int,
    val divisionValue: Int,
    val stateCode: Int,
    val managerId: String?,
    val controlled: Boolean,
    val hasLegacyTState: Boolean,
)

data class LegacyManagerChangeHistoryEntry(
    val outgoingManagerId: String,
    val incomingManagerId: String?,
    val date: LegacyManagerEmploymentDate,
    val previousClubWorldId: Int,
)

data class LegacyManagerEmploymentWorldState(
    /** Legacy `g1()` is an ArrayList: duplicates/order are observable and removal removes the first match. */
    val controlledClubIds: List<String>,
    val rawH3Value: Int?,
    val rawI3Value: Int?,
    val managerChangeHistory: List<LegacyManagerChangeHistoryEntry>,
)

enum class LegacyManagerEmploymentEffect {
    RECORD_MANAGER_CHANGE_HISTORY,
    CAPTURE_OUTGOING_PREVIOUS_CLUB,
    SET_WORLD_H3_FROM_CLUB_STATE,
    SET_WORLD_I3_FROM_CLUB_COUNTRY,
    SET_OUTGOING_CLUB_CONTROLLED_FALSE,
    RESET_OUTGOING_CLUB_J1,
    CLEAR_OUTGOING_CLUB_YOUTH_K1,
    REMOVE_OUTGOING_CLUB_FROM_WORLD_FIRST,
    CLEAR_OUTGOING_MANAGER_R,
    RESET_OUTGOING_CLUB_T_ZERO,
    CLEAR_OUTGOING_CLUB_MANAGER,
    CLEAR_OUTGOING_CURRENT_CLUB,
    ASSIGN_INCOMING_CLUB_MANAGER,
    SET_INCOMING_CURRENT_CLUB,
    SET_INCOMING_G_100,
    SET_INCOMING_H_80,
    SET_INCOMING_M_0,
    RESET_INCOMING_CLUB_J1,
    SET_INCOMING_CLUB_CONTROLLED_TRUE,
    ADD_INCOMING_CLUB_TO_WORLD,
    RESET_INCOMING_ROSTER_N1,
    EXTEND_INCOMING_ROSTER_180_DAYS,
    RESET_AND_CREATE_INCOMING_YOUTH_C1,
}

data class LegacyManagerEmploymentTransitionResult(
    val outgoingManager: LegacyManagerEmploymentState?,
    val incomingManager: LegacyManagerEmploymentState?,
    val targetClub: LegacyManagerEmploymentClubState,
    val world: LegacyManagerEmploymentWorldState,
    val effectsInOrder: List<LegacyManagerEmploymentEffect>,
)

/**
 * Exact persistence-independent projection of the readable legacy chain
 * `best.b.G(target, outgoing, incoming) -> outgoing.l(incoming) -> incoming.e(target)`.
 *
 * Deep helper calls (`j1`, `k1`, `N1`, player `c(180,true)`, `c1`, `T.A(0)`, `R(null)`) stay as
 * ordered typed effects when their internal state is outside this boundary. Direct manager/club/world
 * fields proven by Java+SMALI are mutated here. No replacement-manager selection from `best.c0.y()`
 * is performed by this rule.
 */
object LegacyManagerEmploymentTransitionRule {
    fun transfer(
        targetClub: LegacyManagerEmploymentClubState,
        outgoingManager: LegacyManagerEmploymentState?,
        incomingManager: LegacyManagerEmploymentState?,
        world: LegacyManagerEmploymentWorldState,
        date: LegacyManagerEmploymentDate,
    ): LegacyManagerEmploymentTransitionResult {
        var club = targetClub
        var outgoing = outgoingManager
        var incoming = incomingManager
        var worldState = world
        val effects = mutableListOf<LegacyManagerEmploymentEffect>()

        if (outgoing != null) {
            // `f0.k()` dereferences A() in the legacy implementation; do not silently repair null.
            if (outgoing.currentClubId == null) {
                throw NullPointerException("legacy outgoing manager has no current club")
            }

            worldState = worldState.copy(
                managerChangeHistory = worldState.managerChangeHistory + LegacyManagerChangeHistoryEntry(
                    outgoingManagerId = outgoing.managerId,
                    incomingManagerId = incoming?.managerId,
                    date = date,
                    previousClubWorldId = club.legacyWorldId,
                ),
            )
            effects += LegacyManagerEmploymentEffect.RECORD_MANAGER_CHANGE_HISTORY

            outgoing = outgoing.copy(
                previousClubId = club.clubId,
                previousClubCountry = club.countryCode,
                previousClubDivisionIndex = club.divisionValue - 1,
            )
            effects += LegacyManagerEmploymentEffect.CAPTURE_OUTGOING_PREVIOUS_CLUB

            if (outgoing.isUserControlled) {
                if (club.countryCode == 29) {
                    worldState = worldState.copy(rawH3Value = club.stateCode)
                    effects += LegacyManagerEmploymentEffect.SET_WORLD_H3_FROM_CLUB_STATE
                }
                worldState = worldState.copy(rawI3Value = club.countryCode)
                effects += LegacyManagerEmploymentEffect.SET_WORLD_I3_FROM_CLUB_COUNTRY

                club = club.copy(controlled = false)
                effects += LegacyManagerEmploymentEffect.SET_OUTGOING_CLUB_CONTROLLED_FALSE
                effects += LegacyManagerEmploymentEffect.RESET_OUTGOING_CLUB_J1
                effects += LegacyManagerEmploymentEffect.CLEAR_OUTGOING_CLUB_YOUTH_K1

                val controlled = worldState.controlledClubIds.toMutableList()
                controlled.remove(club.clubId)
                worldState = worldState.copy(controlledClubIds = controlled)
                effects += LegacyManagerEmploymentEffect.REMOVE_OUTGOING_CLUB_FROM_WORLD_FIRST
                effects += LegacyManagerEmploymentEffect.CLEAR_OUTGOING_MANAGER_R
            }

            if (club.hasLegacyTState) {
                effects += LegacyManagerEmploymentEffect.RESET_OUTGOING_CLUB_T_ZERO
            }

            club = club.copy(managerId = null)
            effects += LegacyManagerEmploymentEffect.CLEAR_OUTGOING_CLUB_MANAGER
            outgoing = outgoing.copy(currentClubId = null)
            effects += LegacyManagerEmploymentEffect.CLEAR_OUTGOING_CURRENT_CLUB
        }

        if (incoming != null) {
            club = club.copy(managerId = incoming.managerId)
            effects += LegacyManagerEmploymentEffect.ASSIGN_INCOMING_CLUB_MANAGER
            incoming = incoming.copy(currentClubId = club.clubId)
            effects += LegacyManagerEmploymentEffect.SET_INCOMING_CURRENT_CLUB
            incoming = incoming.copy(rawG = 100)
            effects += LegacyManagerEmploymentEffect.SET_INCOMING_G_100
            incoming = incoming.copy(rawH = 80)
            effects += LegacyManagerEmploymentEffect.SET_INCOMING_H_80
            incoming = incoming.copy(rawM = 0)
            effects += LegacyManagerEmploymentEffect.SET_INCOMING_M_0
            effects += LegacyManagerEmploymentEffect.RESET_INCOMING_CLUB_J1

            if (incoming.isUserControlled) {
                club = club.copy(controlled = true)
                effects += LegacyManagerEmploymentEffect.SET_INCOMING_CLUB_CONTROLLED_TRUE
                worldState = worldState.copy(
                    controlledClubIds = worldState.controlledClubIds + club.clubId,
                )
                effects += LegacyManagerEmploymentEffect.ADD_INCOMING_CLUB_TO_WORLD
                effects += LegacyManagerEmploymentEffect.RESET_INCOMING_ROSTER_N1
                effects += LegacyManagerEmploymentEffect.EXTEND_INCOMING_ROSTER_180_DAYS
                effects += LegacyManagerEmploymentEffect.RESET_AND_CREATE_INCOMING_YOUTH_C1
            }
        }

        return LegacyManagerEmploymentTransitionResult(
            outgoingManager = outgoing,
            incomingManager = incoming,
            targetClub = club,
            world = worldState,
            effectsInOrder = effects,
        )
    }
}
