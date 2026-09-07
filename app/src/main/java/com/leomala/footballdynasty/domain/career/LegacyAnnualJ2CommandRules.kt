package com.leomala.footballdynasty.domain.career

/**
 * Pure dispatcher projection of reachable legacy `best.a.J(1)`.
 *
 * The legacy annual path consumes command strings from `best.a.c` in their existing order and
 * clears that same list after the pass. This rule freezes only that dispatch contract; substantive
 * callees (`q/s/p/r/n` and `best.b.p`) stay independently characterized and must be executed by
 * their own proven runtime boundaries.
 */
object LegacyAnnualJ2CommandRules {
    enum class Action {
        CALL_Q,
        CALL_S,
        CALL_B_P,
        CALL_P,
        CALL_R,
        CALL_N_FALSE,
        CALL_N_TRUE,
        CLEAR_COMMANDS,
    }

    /**
     * Mirrors SMALI `best.a.J(1)` for the annual command list.
     *
     * Recognized commands are dispatched in list order. `cD` calls `p()` only when the legacy
     * `Y1()` guard is true. `cO` and unknown strings are no-ops in this dispatcher. The command
     * list is always cleared after iteration, including when it is empty or contains only no-ops.
     */
    fun plan(
        commands: List<String>,
        legacyY1: Boolean,
    ): List<Action> = buildList {
        commands.forEach { command ->
            when (command) {
                "cw" -> add(Action.CALL_Q)
                "ds" -> add(Action.CALL_S)
                "aj" -> add(Action.CALL_B_P)
                "cD" -> if (legacyY1) add(Action.CALL_P)
                "dJ" -> add(Action.CALL_R)
                "cS" -> add(Action.CALL_N_FALSE)
                "cO" -> Unit
                "cSempregado" -> add(Action.CALL_N_TRUE)
            }
        }
        add(Action.CLEAR_COMMANDS)
    }
}
