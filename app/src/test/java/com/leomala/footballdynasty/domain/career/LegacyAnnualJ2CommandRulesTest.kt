package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualJ2CommandRules.Action
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualJ2CommandRulesTest {
    @Test
    fun `annual commands preserve list order and clear after dispatch`() {
        val actions = LegacyAnnualJ2CommandRules.plan(
            commands = listOf("cw", "ds", "aj", "cD", "dJ", "cS", "cO", "cSempregado"),
            legacyY1 = true,
        )

        assertEquals(
            listOf(
                Action.CALL_Q,
                Action.CALL_S,
                Action.CALL_B_P,
                Action.CALL_P,
                Action.CALL_R,
                Action.CALL_N_FALSE,
                Action.CALL_N_TRUE,
                Action.CLEAR_COMMANDS,
            ),
            actions,
        )
    }

    @Test
    fun `cD is skipped when legacy Y1 guard is false`() {
        val actions = LegacyAnnualJ2CommandRules.plan(
            commands = listOf("cw", "cD", "dJ"),
            legacyY1 = false,
        )

        assertEquals(
            listOf(Action.CALL_Q, Action.CALL_R, Action.CLEAR_COMMANDS),
            actions,
        )
    }

    @Test
    fun `cO and unknown commands are no ops but list is still cleared`() {
        val actions = LegacyAnnualJ2CommandRules.plan(
            commands = listOf("cO", "unknown", ""),
            legacyY1 = true,
        )

        assertEquals(listOf(Action.CLEAR_COMMANDS), actions)
    }

    @Test
    fun `empty command list still reaches one shot clear`() {
        assertEquals(
            listOf(Action.CLEAR_COMMANDS),
            LegacyAnnualJ2CommandRules.plan(emptyList(), legacyY1 = false),
        )
    }
}
