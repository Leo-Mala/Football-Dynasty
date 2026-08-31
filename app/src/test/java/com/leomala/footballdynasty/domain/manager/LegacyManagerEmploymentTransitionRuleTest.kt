package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyManagerEmploymentTransitionRuleTest {
    private val date = LegacyManagerEmploymentDate(2026, 7, 31)

    private fun club(
        managerId: String? = "old",
        controlled: Boolean = true,
        countryCode: Int = 29,
        stateCode: Int = 13,
        hasT: Boolean = true,
    ) = LegacyManagerEmploymentClubState(
        clubId = "club-A",
        legacyWorldId = 77,
        countryCode = countryCode,
        divisionValue = 3,
        stateCode = stateCode,
        managerId = managerId,
        controlled = controlled,
        hasLegacyTState = hasT,
    )

    private fun manager(
        id: String,
        user: Boolean,
        clubId: String? = null,
        g: Int = 1,
        h: Int = 2,
        m: Int = 3,
    ) = LegacyManagerEmploymentState(
        managerId = id,
        isUserControlled = user,
        currentClubId = clubId,
        previousClubId = null,
        previousClubCountry = null,
        previousClubDivisionIndex = null,
        rawG = g,
        rawH = h,
        rawM = m,
    )

    private fun world(ids: List<String> = listOf("club-A", "other", "club-A")) =
        LegacyManagerEmploymentWorldState(
            controlledClubIds = ids,
            rawH3Value = null,
            rawI3Value = null,
            managerChangeHistory = emptyList(),
        )

    @Test
    fun userManagerDepartureThenUserManagerArrivalPreservesLegacyOrderAndArrayListSemantics() {
        val result = LegacyManagerEmploymentTransitionRule.transfer(
            targetClub = club(),
            outgoingManager = manager("old", user = true, clubId = "club-A"),
            incomingManager = manager("new", user = true),
            world = world(),
            date = date,
        )

        assertEquals(
            listOf(
                LegacyManagerEmploymentEffect.RECORD_MANAGER_CHANGE_HISTORY,
                LegacyManagerEmploymentEffect.CAPTURE_OUTGOING_PREVIOUS_CLUB,
                LegacyManagerEmploymentEffect.SET_WORLD_H3_FROM_CLUB_STATE,
                LegacyManagerEmploymentEffect.SET_WORLD_I3_FROM_CLUB_COUNTRY,
                LegacyManagerEmploymentEffect.SET_OUTGOING_CLUB_CONTROLLED_FALSE,
                LegacyManagerEmploymentEffect.RESET_OUTGOING_CLUB_J1,
                LegacyManagerEmploymentEffect.CLEAR_OUTGOING_CLUB_YOUTH_K1,
                LegacyManagerEmploymentEffect.REMOVE_OUTGOING_CLUB_FROM_WORLD_FIRST,
                LegacyManagerEmploymentEffect.CLEAR_OUTGOING_MANAGER_R,
                LegacyManagerEmploymentEffect.RESET_OUTGOING_CLUB_T_ZERO,
                LegacyManagerEmploymentEffect.CLEAR_OUTGOING_CLUB_MANAGER,
                LegacyManagerEmploymentEffect.CLEAR_OUTGOING_CURRENT_CLUB,
                LegacyManagerEmploymentEffect.ASSIGN_INCOMING_CLUB_MANAGER,
                LegacyManagerEmploymentEffect.SET_INCOMING_CURRENT_CLUB,
                LegacyManagerEmploymentEffect.SET_INCOMING_G_100,
                LegacyManagerEmploymentEffect.SET_INCOMING_H_80,
                LegacyManagerEmploymentEffect.SET_INCOMING_M_0,
                LegacyManagerEmploymentEffect.RESET_INCOMING_CLUB_J1,
                LegacyManagerEmploymentEffect.SET_INCOMING_CLUB_CONTROLLED_TRUE,
                LegacyManagerEmploymentEffect.ADD_INCOMING_CLUB_TO_WORLD,
                LegacyManagerEmploymentEffect.RESET_INCOMING_ROSTER_N1,
                LegacyManagerEmploymentEffect.EXTEND_INCOMING_ROSTER_180_DAYS,
                LegacyManagerEmploymentEffect.RESET_AND_CREATE_INCOMING_YOUTH_C1,
            ),
            result.effectsInOrder,
        )
        assertEquals("club-A", result.outgoingManager!!.previousClubId)
        assertEquals(29, result.outgoingManager.previousClubCountry)
        assertEquals(2, result.outgoingManager.previousClubDivisionIndex)
        assertNull(result.outgoingManager.currentClubId)
        assertEquals("club-A", result.incomingManager!!.currentClubId)
        assertEquals(100, result.incomingManager.rawG)
        assertEquals(80, result.incomingManager.rawH)
        assertEquals(0, result.incomingManager.rawM)
        assertEquals("new", result.targetClub.managerId)
        assertTrue(result.targetClub.controlled)
        // remove(Object) removes the first match; the incoming user is then appended.
        assertEquals(listOf("other", "club-A", "club-A"), result.world.controlledClubIds)
        assertEquals(13, result.world.rawH3Value)
        assertEquals(29, result.world.rawI3Value)
        assertEquals(
            LegacyManagerChangeHistoryEntry("old", "new", date, 77),
            result.world.managerChangeHistory.single(),
        )
    }

    @Test
    fun nonUserDepartureDoesNotRunUserOnlyWorldAndClubResetBranch() {
        val result = LegacyManagerEmploymentTransitionRule.transfer(
            targetClub = club(controlled = false, countryCode = 10),
            outgoingManager = manager("old", user = false, clubId = "club-A"),
            incomingManager = null,
            world = world(listOf("other")),
            date = date,
        )

        assertFalse(result.effectsInOrder.contains(LegacyManagerEmploymentEffect.SET_WORLD_H3_FROM_CLUB_STATE))
        assertFalse(result.effectsInOrder.contains(LegacyManagerEmploymentEffect.SET_WORLD_I3_FROM_CLUB_COUNTRY))
        assertFalse(result.effectsInOrder.contains(LegacyManagerEmploymentEffect.RESET_OUTGOING_CLUB_J1))
        assertFalse(result.effectsInOrder.contains(LegacyManagerEmploymentEffect.CLEAR_OUTGOING_CLUB_YOUTH_K1))
        assertTrue(result.effectsInOrder.contains(LegacyManagerEmploymentEffect.RESET_OUTGOING_CLUB_T_ZERO))
        assertNull(result.targetClub.managerId)
        assertNull(result.outgoingManager!!.currentClubId)
        assertEquals(listOf("other"), result.world.controlledClubIds)
    }

    @Test
    fun incomingNonUserGetsClubAndRawDefaultsWithoutUserOnlyInitialization() {
        val result = LegacyManagerEmploymentTransitionRule.transfer(
            targetClub = club(managerId = null, controlled = false, hasT = false),
            outgoingManager = null,
            incomingManager = manager("new", user = false),
            world = world(emptyList()),
            date = date,
        )

        assertEquals("new", result.targetClub.managerId)
        assertFalse(result.targetClub.controlled)
        assertEquals("club-A", result.incomingManager!!.currentClubId)
        assertEquals(100, result.incomingManager.rawG)
        assertEquals(80, result.incomingManager.rawH)
        assertEquals(0, result.incomingManager.rawM)
        assertFalse(result.effectsInOrder.contains(LegacyManagerEmploymentEffect.SET_INCOMING_CLUB_CONTROLLED_TRUE))
        assertFalse(result.effectsInOrder.contains(LegacyManagerEmploymentEffect.ADD_INCOMING_CLUB_TO_WORLD))
        assertTrue(result.world.managerChangeHistory.isEmpty())
    }

    @Test
    fun h3IsWrittenOnlyForLegacyCountry29ButI3IsWrittenForEveryUserDeparture() {
        val result = LegacyManagerEmploymentTransitionRule.transfer(
            targetClub = club(countryCode = 8, stateCode = 99),
            outgoingManager = manager("old", user = true, clubId = "club-A"),
            incomingManager = null,
            world = world(),
            date = date,
        )

        assertNull(result.world.rawH3Value)
        assertEquals(8, result.world.rawI3Value)
        assertFalse(result.effectsInOrder.contains(LegacyManagerEmploymentEffect.SET_WORLD_H3_FROM_CLUB_STATE))
        assertTrue(result.effectsInOrder.contains(LegacyManagerEmploymentEffect.SET_WORLD_I3_FROM_CLUB_COUNTRY))
    }

    @Test(expected = NullPointerException::class)
    fun outgoingManagerWithoutCurrentClubIsNotSilentlyRepaired() {
        LegacyManagerEmploymentTransitionRule.transfer(
            targetClub = club(),
            outgoingManager = manager("old", user = true, clubId = null),
            incomingManager = null,
            world = world(),
            date = date,
        )
    }
}
