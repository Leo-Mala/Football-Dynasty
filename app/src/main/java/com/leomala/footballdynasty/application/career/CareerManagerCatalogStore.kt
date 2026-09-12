package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.data.local.CareerCoachRuntimeStore
import com.leomala.footballdynasty.data.local.CareerTicketRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.domain.manager.LegacyCoachSeasonClubRecord

/**
 * Read-only Phase 17 projection of the persisted manager attached to the managed club.
 *
 * This boundary deliberately preserves legacy runtime fields as opaque raw values. It does not
 * rename G/H/D/E/F/O/M into inferred gameplay concepts, synthesize a missing manager, or repair
 * inconsistent employment state. Missing V9/V11 materialization fails closed.
 */
class CareerManagerCatalogStore(
    private val database: FootballDynastyDatabase,
    private val ticketStore: CareerTicketRuntimeStore = CareerTicketRuntimeStore(database),
    private val coachStore: CareerCoachRuntimeStore = CareerCoachRuntimeStore(database),
) {
    data class ManagerSnapshot(
        val careerId: String,
        val clubId: String,
        val clubName: String,
        val sourceOrdinal: Int,
        val legacyManagerId: Int,
        val isUserControlled: Boolean,
        val rawG: Int,
        val rawH: Int,
        val rawD: Int,
        val rawE: Int,
        val rawF: Int,
        val rawO: Int,
        val rawM: Int,
        val records: List<LegacyCoachSeasonClubRecord>,
    )

    suspend fun loadManager(careerId: String): ManagerSnapshot? {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        val core = database.careerCoreStateDao().findById(careerId) ?: return null
        val clubId = core.managedClubId ?: return null
        val club = database.clubDao().findById(clubId) ?: return null
        val clubState = ticketStore.findClubState(careerId, clubId) ?: return null

        // Preserve `best.b.b1(id)` first-match semantics without turning an incomplete V11 slice
        // into a presentation crash: a missing ordered parent or coach row is simply unavailable.
        val managerParent = ticketStore.managersInWorldOrder(careerId)
            .firstOrNull { it.legacyManagerId == clubState.legacyManagerId }
            ?: return null
        val coach = coachStore.find(careerId, managerParent.sourceOrdinal) ?: return null

        // The club->manager id and the V11 coach employment link must describe the same persisted
        // relationship. A stale/dismissed/incomplete slice is not repaired by the presentation layer.
        if (coach.legacyManagerId != clubState.legacyManagerId || coach.currentClubId != clubId) {
            return null
        }

        return ManagerSnapshot(
            careerId = careerId,
            clubId = clubId,
            clubName = club.name,
            sourceOrdinal = coach.sourceOrdinal,
            legacyManagerId = coach.legacyManagerId,
            isUserControlled = coach.isUserControlled,
            rawG = coach.rawG,
            rawH = coach.rawH,
            rawD = coach.rawD,
            rawE = coach.rawE,
            rawF = coach.rawF,
            rawO = coach.rawO,
            rawM = coach.rawM,
            records = coach.records.toList(),
        )
    }
}
