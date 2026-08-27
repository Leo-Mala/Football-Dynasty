package com.leomala.footballdynasty.domain.match

/**
 * Pure routing for the proven post-loop tail of automatic legacy `best.s.Q0()`.
 *
 * After both automatic half loops, legacy evaluates `Z && a0 && P0()` with Java short-circuit
 * semantics; only a true result reaches `o()`. Independently of that gate result, the two proven
 * per-club flags are then cleared. This rule returns that mutation/call plan only: it does not
 * assign sporting meaning to the obfuscated flags and does not implement the still-open effects of
 * `o()` itself.
 */
object LegacyMatchAutomaticPostSimulationRules {
    data class Result(
        val evaluatedP0: Boolean,
        val invokeLegacyO: Boolean,
        val clearFirstClubFlag: Boolean = true,
        val clearSecondClubFlag: Boolean = true,
    )

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

        return Result(
            evaluatedP0 = evaluatedP0,
            invokeLegacyO = invokeLegacyO,
        )
    }
}
