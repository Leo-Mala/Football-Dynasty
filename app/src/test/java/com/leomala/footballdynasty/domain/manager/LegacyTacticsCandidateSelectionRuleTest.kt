package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyTacticsCandidateSelectionRuleTest {
    private fun player(
        id: String,
        position: Int,
        subrole: Int,
        skill: Int = 50,
        star: Boolean = false,
    ) = LegacyTacticsRuntimePlayer(id, position, subrole, skill, star)

    @Test
    fun cornerTakersExcludeOnlyGoalkeepersAndPreserveCandidateOrder() {
        val roster = listOf(
            player("A", 4, 1),
            player("GK", 0, 0),
            player("B", 3, 0),
        )
        val result = LegacyTacticsCandidateSelectionRule.select("bEscanteios", roster)

        assertEquals(listOf("A", "B"), result.candidates.map { it.value })
        assertEquals(listOf("GK", "B", "A"), result.rosterAfterLegacySort.map { it.value })
    }

    @Test
    fun falseNineAllowsAttackersAndSubroleOneMidfieldersOnly() {
        val roster = listOf(
            player("ATT", 4, 0),
            player("OM", 3, 1),
            player("DM", 3, 0),
            player("DEF", 2, 0),
        )
        val result = LegacyTacticsCandidateSelectionRule.select("fNove", roster)

        assertEquals(listOf("ATT", "OM"), result.candidates.map { it.value })
    }

    @Test
    fun unknownActionUsesEntireRosterWithoutInventingAnotherFilter() {
        val roster = listOf(player("B", 4, 0), player("A", 0, 0))
        val result = LegacyTacticsCandidateSelectionRule.select("unknown", roster)

        assertEquals(listOf("B", "A"), result.candidates.map { it.value })
        assertEquals(listOf("A", "B"), result.rosterAfterLegacySort.map { it.value })
    }

    @Test
    fun legacySortUsesPositionThenSubroleThenSkillThenStar() {
        val roster = listOf(
            player("lowSkill", 3, 1, skill = 60, star = false),
            player("starTie", 3, 1, skill = 80, star = true),
            player("plainTie", 3, 1, skill = 80, star = false),
            player("defMid", 3, 0, skill = 10, star = false),
            player("attacker", 4, 0, skill = 99, star = true),
        )
        val result = LegacyTacticsCandidateSelectionRule.select("other", roster)

        assertEquals(
            listOf("defMid", "starTie", "plainTie", "lowSkill", "attacker"),
            result.rosterAfterLegacySort.map { it.value },
        )
    }

    @Test
    fun exactComparatorTiesRemainStable() {
        val roster = listOf(
            player("first", 3, 1, 80, true),
            player("second", 3, 1, 80, true),
        )
        val result = LegacyTacticsCandidateSelectionRule.select("other", roster)

        assertEquals(listOf("first", "second"), result.rosterAfterLegacySort.map { it.value })
    }
}
