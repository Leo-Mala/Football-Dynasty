package com.leomala.footballdynasty.domain.manager

/**
 * Exact UI-dispatch ordering from `ActivityConvite.onClickAccept/onClickCancel` in the official
 * legacy corpus.
 *
 * This rule intentionally stops before the internals of `c0.y()` and `best.b.G(...)`: those calls
 * are represented as ordered invocations so their remaining manager/club mutations can be
 * characterized independently without inventing semantics.
 */
enum class LegacyClubInvitationInvocation {
    RESOLVE_CURRENT_CLUB_MANAGER,
    ASSIGN_INCOMING_MANAGER_TO_TARGET_WITH_CURRENT_TARGET_MANAGER,
    CLEAR_PENDING_CLUB_OFFERS,
    FINISH_ACTIVITY,
    CONTINUE_CAREER,
}

data class LegacyClubInvitationActionPlan(
    val selectedOfferIndex: Int?,
    val invocations: List<LegacyClubInvitationInvocation>,
)

object LegacyClubInvitationActionRule {
    const val legacyOptionSlots: Int = 6

    fun accept(
        checkedSlots: List<Boolean>,
        offerCount: Int,
        managerPresent: Boolean,
        managerHasCurrentClub: Boolean,
    ): LegacyClubInvitationActionPlan {
        require(checkedSlots.size == legacyOptionSlots) {
            "ActivityConvite has exactly $legacyOptionSlots radio-button slots"
        }
        require(offerCount >= 0) { "ArrayList.size() cannot be negative" }

        val selectedIndex = checkedSlots.indexOfFirst { checked -> checked }.takeIf { it >= 0 }
        val validSelection = selectedIndex != null && selectedIndex < offerCount && managerPresent

        val invocations = buildList {
            if (validSelection) {
                if (managerHasCurrentClub) {
                    add(LegacyClubInvitationInvocation.RESOLVE_CURRENT_CLUB_MANAGER)
                }
                add(
                    LegacyClubInvitationInvocation
                        .ASSIGN_INCOMING_MANAGER_TO_TARGET_WITH_CURRENT_TARGET_MANAGER,
                )
            }
            add(LegacyClubInvitationInvocation.FINISH_ACTIVITY)
            add(LegacyClubInvitationInvocation.CONTINUE_CAREER)
        }

        return LegacyClubInvitationActionPlan(
            selectedOfferIndex = selectedIndex,
            invocations = invocations,
        )
    }

    fun cancel(): LegacyClubInvitationActionPlan = LegacyClubInvitationActionPlan(
        selectedOfferIndex = null,
        invocations = listOf(
            LegacyClubInvitationInvocation.CLEAR_PENDING_CLUB_OFFERS,
            LegacyClubInvitationInvocation.FINISH_ACTIVITY,
            LegacyClubInvitationInvocation.CONTINUE_CAREER,
        ),
    )
}
