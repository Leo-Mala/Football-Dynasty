package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity

/**
 * Durable owners for the `u0() != null` and `u0().Q0()` inputs used by
 * `ActivityEscalacao.K0()`.
 *
 * A prepared senior player is reached through `career_squad_memberships`, so a
 * matching membership proves that the legacy player has a current club for the
 * prepared roster. The club `Q0()` value is read only from the explicitly
 * materialized career-local club runtime. Missing or inconsistent runtime state
 * stays unresolved instead of being interpreted as `false`.
 */
data class CareerLineupClubEligibilityOwner(
    val clubId: String,
    val hasClub: Boolean,
    val clubActiveQ0: Boolean,
)

class CareerLineupClubEligibilityOwnerStore(
    private val database: FootballDynastyDatabase,
) {
    suspend fun resolveForPreparedPlayer(
        careerId: String,
        playerId: String,
    ): CareerLineupClubEligibilityOwner? {
        val membership = database.careerPlayerRuntimeDao().findMembership(careerId, playerId)
            ?: return null
        val clubRuntime = database.careerManagerRuntimeDao()
            .findClubRuntime(careerId, membership.clubId)

        return resolvePersisted(
            careerId = careerId,
            membership = membership,
            clubRuntime = clubRuntime,
        )
    }

    internal companion object {
        fun resolvePersisted(
            careerId: String,
            membership: CareerSquadMembershipEntity,
            clubRuntime: CareerClubManagerRuntimeEntity?,
        ): CareerLineupClubEligibilityOwner? {
            if (membership.careerId != careerId) return null
            if (clubRuntime == null) return null
            if (clubRuntime.careerId != careerId) return null
            if (clubRuntime.clubId != membership.clubId) return null

            return CareerLineupClubEligibilityOwner(
                clubId = membership.clubId,
                hasClub = true,
                clubActiveQ0 = clubRuntime.active,
            )
        }
    }
}
