package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.model.Club

/**
 * Resolves the club explicitly controlled by the current career.
 *
 * The persisted managed-club id is authoritative. A missing or stale id does
 * not trigger a fallback to another club, because the legacy evidence does not
 * prove any automatic replacement behavior at this boundary.
 */
object ManagedClubSelection {
    fun resolve(
        career: CareerState,
        clubs: List<Club>,
    ): Club? {
        val managedClubId = career.managedClub?.clubId ?: return null
        return clubs.firstOrNull { it.id == managedClubId }
    }
}
