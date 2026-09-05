package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualPlayerProgressionSweepRules.Action
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LegacyAnnualPlayerProgressionSweepRulesTest {
    @Test
    fun `all senior players progress before any junior draft`() {
        val actions = LegacyAnnualPlayerProgressionSweepRules.plan(
            LegacyAnnualPlayerProgressionSweepRules.Input(
                seniorPlayerCount = 3,
                juniorDraftCountsByClub = listOf(2, 1),
            ),
        )

        assertEquals(
            listOf(
                Action.ProgressSeniorPlayer(0),
                Action.ProgressSeniorPlayer(1),
                Action.ProgressSeniorPlayer(2),
                Action.ProgressJuniorDraft(clubIndex = 0, juniorIndex = 0),
                Action.ProgressJuniorDraft(clubIndex = 0, juniorIndex = 1),
                Action.ProgressJuniorDraft(clubIndex = 1, juniorIndex = 0),
            ),
            actions,
        )
    }

    @Test
    fun `junior sweep preserves club order and source order inside each club`() {
        val actions = LegacyAnnualPlayerProgressionSweepRules.plan(
            LegacyAnnualPlayerProgressionSweepRules.Input(
                seniorPlayerCount = 0,
                juniorDraftCountsByClub = listOf(1, 0, 2),
            ),
        )

        assertEquals(
            listOf(
                Action.ProgressJuniorDraft(0, 0),
                Action.ProgressJuniorDraft(2, 0),
                Action.ProgressJuniorDraft(2, 1),
            ),
            actions,
        )
    }

    @Test
    fun `empty global collections produce no actions`() {
        assertEquals(
            emptyList<Action>(),
            LegacyAnnualPlayerProgressionSweepRules.plan(
                LegacyAnnualPlayerProgressionSweepRules.Input(
                    seniorPlayerCount = 0,
                    juniorDraftCountsByClub = emptyList(),
                ),
            ),
        )
    }

    @Test
    fun `input rejects negative counts`() {
        assertThrows(IllegalArgumentException::class.java) {
            LegacyAnnualPlayerProgressionSweepRules.Input(
                seniorPlayerCount = -1,
                juniorDraftCountsByClub = emptyList(),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            LegacyAnnualPlayerProgressionSweepRules.Input(
                seniorPlayerCount = 0,
                juniorDraftCountsByClub = listOf(1, -1),
            )
        }
    }
}
