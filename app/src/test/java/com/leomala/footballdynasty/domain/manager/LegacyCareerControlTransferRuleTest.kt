package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerControlTransferRuleTest {
    private class Manager(val id: String, var controlled: Boolean)
    private class Club(val name: String, val manager: Manager, var controlled: Boolean)

    @Test
    fun resumeRuleDispatchesExactlyOneLegacyBranch() {
        val calls = mutableListOf<String>()
        val end = LegacyCareerResumeRule.dispatch(
            legacyEndOfYearFlag = { true },
            runEndOfYearFinalizer = { calls += "d" },
            continueCareer = { calls += "i" },
        )
        assertEquals(LegacyCareerResumeEffect.RUN_END_OF_YEAR_FINALIZER, end)
        assertEquals(listOf("d"), calls)

        calls.clear()
        val normal = LegacyCareerResumeRule.dispatch(
            legacyEndOfYearFlag = { false },
            runEndOfYearFinalizer = { calls += "d" },
            continueCareer = { calls += "i" },
        )
        assertEquals(LegacyCareerResumeEffect.CONTINUE_CAREER, normal)
        assertEquals(listOf("i"), calls)
    }

    @Test
    fun switchUsesFirstControlledAndFirstMatchingClubThenReplacesManagerListInOrder() {
        val oldManager = Manager("old-manager", true)
        val newManager = Manager("new-manager", false)
        val laterManager = Manager("later-manager", false)
        val oldClub = Club("Old", oldManager, true)
        val newClub = Club("New", newManager, false)
        val laterDuplicate = Club("New", laterManager, false)
        val managerList = mutableListOf(oldManager, laterManager)
        val calls = mutableListOf<String>()

        val result = LegacyControlledClubSwitchRule.switchByName(
            clubs = listOf(oldClub, newClub, laterDuplicate),
            requestedClubName = "New",
            isControlledClub = { it.controlled },
            clubName = { it.name },
            managerOfClub = { it.manager },
            setClubControlled = { club, controlled -> calls += "club:${club.name}:$controlled"; club.controlled = controlled },
            setManagerControlled = { manager, controlled -> calls += "manager:${manager.id}:$controlled"; manager.controlled = controlled },
            clearManagerList = { calls += "clear"; managerList.clear() },
            addManager = { calls += "add:${it.id}"; managerList += it },
        )

        assertSame(oldClub, result.previousClub)
        assertSame(newClub, result.selectedClub)
        assertSame(newManager, result.selectedManager)
        assertEquals(
            listOf(
                LegacyControlledClubSwitchEffect.OLD_CLUB_UNCONTROLLED,
                LegacyControlledClubSwitchEffect.OLD_MANAGER_UNCONTROLLED,
                LegacyControlledClubSwitchEffect.NEW_CLUB_CONTROLLED,
                LegacyControlledClubSwitchEffect.NEW_MANAGER_CONTROLLED,
                LegacyControlledClubSwitchEffect.MANAGER_LIST_CLEARED,
                LegacyControlledClubSwitchEffect.NEW_MANAGER_ADDED,
            ),
            result.effectsInOrder,
        )
        assertEquals(
            listOf(
                "club:Old:false",
                "manager:old-manager:false",
                "club:New:true",
                "manager:new-manager:true",
                "clear",
                "add:new-manager",
            ),
            calls,
        )
        assertFalse(oldClub.controlled)
        assertFalse(oldManager.controlled)
        assertTrue(newClub.controlled)
        assertTrue(newManager.controlled)
        assertEquals(listOf(newManager), managerList)
        assertFalse(laterDuplicate.controlled)
    }

    @Test
    fun missingOutgoingOrTargetPerformsNoMutation() {
        val manager = Manager("m", false)
        val club = Club("Only", manager, false)
        var mutations = 0

        val result = LegacyControlledClubSwitchRule.switchByName(
            clubs = listOf(club),
            requestedClubName = "Missing",
            isControlledClub = { it.controlled },
            clubName = { it.name },
            managerOfClub = { it.manager },
            setClubControlled = { _, _ -> mutations++ },
            setManagerControlled = { _, _ -> mutations++ },
            clearManagerList = { mutations++ },
            addManager = { mutations++ },
        )

        assertNull(result.previousClub)
        assertNull(result.selectedClub)
        assertNull(result.selectedManager)
        assertTrue(result.effectsInOrder.isEmpty())
        assertEquals(0, mutations)
    }

    @Test
    fun selfSwitchPreservesLegacyFalseThenTrueSequenceAndManagerListReplacement() {
        val manager = Manager("same-manager", true)
        val club = Club("Same", manager, true)
        val managerList = mutableListOf(manager)
        val calls = mutableListOf<String>()

        val result = LegacyControlledClubSwitchRule.switchByName(
            clubs = listOf(club),
            requestedClubName = "Same",
            isControlledClub = { it.controlled },
            clubName = { it.name },
            managerOfClub = { it.manager },
            setClubControlled = { _, value -> calls += "club:$value"; club.controlled = value },
            setManagerControlled = { _, value -> calls += "manager:$value"; manager.controlled = value },
            clearManagerList = { calls += "clear"; managerList.clear() },
            addManager = { calls += "add"; managerList += it },
        )

        assertSame(club, result.previousClub)
        assertSame(club, result.selectedClub)
        assertEquals(listOf("club:false", "manager:false", "club:true", "manager:true", "clear", "add"), calls)
        assertTrue(club.controlled)
        assertTrue(manager.controlled)
        assertEquals(listOf(manager), managerList)
    }

    @Test
    fun nullRequestedNameMatchesNoNonNullLegacyClubName() {
        val manager = Manager("m", true)
        val club = Club("Name", manager, true)
        val result = LegacyControlledClubSwitchRule.switchByName(
            clubs = listOf(club),
            requestedClubName = null,
            isControlledClub = { it.controlled },
            clubName = { it.name },
            managerOfClub = { it.manager },
            setClubControlled = { _, _ -> error("must not mutate") },
            setManagerControlled = { _, _ -> error("must not mutate") },
            clearManagerList = { error("must not mutate") },
            addManager = { error("must not mutate") },
        )
        assertSame(club, result.previousClub)
        assertNull(result.selectedClub)
        assertTrue(result.effectsInOrder.isEmpty())
    }
}
