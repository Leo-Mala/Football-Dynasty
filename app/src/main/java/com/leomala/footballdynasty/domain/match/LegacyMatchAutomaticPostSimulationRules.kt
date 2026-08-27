package com.leomala.footballdynasty.domain.match

/**
 * Pure routing for the proven post-loop tail of automatic legacy `best.s.Q0()`.
 *
 * After both automatic half loops, legacy evaluates `Z && a0 && P0()` with Java short-circuit
 * semantics; only a true result reaches `o()`. After that gate, both proven per-club flags are
 * cleared. This rule returns that ordered call/mutation plan only: it does not assign sporting
 * meaning to the obfuscated flags, does not claim an order between the two individual flag writes,
 * and does not implement the still-open effects of `o()` itself.
 */
object LegacyMatchAutomaticPostSimulationRules {
    enum class Operation {
        INVOKE_LEGACY_O,
        CLEAR_BOTH_CLUB_FLAGS,
    }

    data class Result(
        val evaluatedP0: Boolean,
        val operations: List<Operation>,
    ) {
        val invokeLegacyO: Boolean
            get() = Operation.INVOKE_LEGACY_O in operations

        val clearBothClubFlags: Boolean
            get() = Operation.CLEAR_BOTH_CLUB_FLAGS in operations
    }

    fun resolve(
        legacyZFlag: Boolean,
        legacyA0Flag: Boolean,
        resolveP0: () -> Boolean,
    ): Result {
        var evaluatedP0 = false
        val invokeLegacyO =
            if (legacyZFlag && legacyA0Flag) {
                evaluatedP0 = true
                resolveP0()
            } else {
                false
            }

        val operations = buildList {
            if (invokeLegacyO) {
                add(Operation.INVOKE_LEGACY_O)
            }
            add(Operation.CLEAR_BOTH_CLUB_FLAGS)
        }

        return Result(
            evaluatedP0 = evaluatedP0,
            operations = operations,
        )
    }
}
