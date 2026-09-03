package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyClubInvitationActionRuleTest {
    @Test
    fun firstCheckedSlotWinsExactlyLikeTheLegacyLoop() {
        val plan = LegacyClubInvitationActionRule.accept(
            checkedSlots = listOf(false, true, false, true, false, false),
            offerCount = 6,
            managerPresent = true,
            managerHasCurrentClub = false,
        )

        assertEquals(1, plan.selectedOfferIndex)
        assertEquals(
            listOf(
                LegacyClubInvitationInvocation
                    .ASSIGN_INCOMING_MANAGER_TO_TARGET_WITH_CURRENT_TARGET_MANAGER,
                LegacyClubInvitationInvocation.FINISH_ACTIVITY,
                LegacyClubInvitationInvocation.CONTINUE_CAREER,
            ),
            plan.invocations,
        )
    }

    @Test
    fun currentClubResolutionHappensBeforeTargetAssignment() {
        val plan = LegacyClubInvitationActionRule.accept(
            checkedSlots = listOf(true, false, false, false, false, false),
            offerCount = 1,
            managerPresent = true,
            managerHasCurrentClub = true,
        )

        assertEquals(
            listOf(
                LegacyClubInvitationInvocation.RESOLVE_CURRENT_CLUB_MANAGER,
                LegacyClubInvitationInvocation
                    .ASSIGN_INCOMING_MANAGER_TO_TARGET_WITH_CURRENT_TARGET_MANAGER,
                LegacyClubInvitationInvocation.FINISH_ACTIVITY,
                LegacyClubInvitationInvocation.CONTINUE_CAREER,
            ),
            plan.invocations,
        )
    }

    @Test
    fun noCheckedSlotStillFinishesAndContinuesWithoutClubMutation() {
        val plan = LegacyClubInvitationActionRule.accept(
            checkedSlots = List(6) { false },
            offerCount = 6,
            managerPresent = true,
            managerHasCurrentClub = true,
        )

        assertEquals(null, plan.selectedOfferIndex)
        assertEquals(
            listOf(
                LegacyClubInvitationInvocation.FINISH_ACTIVITY,
                LegacyClubInvitationInvocation.CONTINUE_CAREER,
            ),
            plan.invocations,
        )
    }

    @Test
    fun outOfRangeSelectionOrMissingManagerDoesNotMutateClubAssignment() {
        val outOfRange = LegacyClubInvitationActionRule.accept(
            checkedSlots = listOf(false, false, true, false, false, false),
            offerCount = 2,
            managerPresent = true,
            managerHasCurrentClub = true,
        )
        val missingManager = LegacyClubInvitationActionRule.accept(
            checkedSlots = listOf(true, false, false, false, false, false),
            offerCount = 1,
            managerPresent = false,
            managerHasCurrentClub = true,
        )

        val terminalOnly = listOf(
            LegacyClubInvitationInvocation.FINISH_ACTIVITY,
            LegacyClubInvitationInvocation.CONTINUE_CAREER,
        )
        assertEquals(terminalOnly, outOfRange.invocations)
        assertEquals(terminalOnly, missingManager.invocations)
    }

    @Test
    fun cancelClearsPendingOffersBeforeFinishingAndContinuing() {
        assertEquals(
            LegacyClubInvitationActionPlan(
                selectedOfferIndex = null,
                invocations = listOf(
                    LegacyClubInvitationInvocation.CLEAR_PENDING_CLUB_OFFERS,
                    LegacyClubInvitationInvocation.FINISH_ACTIVITY,
                    LegacyClubInvitationInvocation.CONTINUE_CAREER,
                ),
            ),
            LegacyClubInvitationActionRule.cancel(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAButtonShapeThatTheLegacyActivityCannotHave() {
        LegacyClubInvitationActionRule.accept(
            checkedSlots = listOf(true),
            offerCount = 1,
            managerPresent = true,
            managerHasCurrentClub = false,
        )
    }
}
