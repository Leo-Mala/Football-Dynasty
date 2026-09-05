package com.leomala.footballdynasty.domain.career

/**
 * Pure projection of reachable legacy `best.o.t()` for annual senior-player decline.
 *
 * The legacy caller `best.o.e()` invokes this path only when the player's age field `e >= 32`.
 * This rule intentionally receives the already-proven primitive inputs instead of assigning new
 * sporting semantics to still-obfuscated club fields.
 *
 * Executable authority: official `best/o.smali` from the pinned Brasfoot corpus.
 */
object LegacyAnnualSeniorDeclineRules {
    data class Input(
        val age: Int,
        val overall: Int,
        val legacyN: Double,
        val clubO: Int,
        val clubF0: Int,
        val clubR0: Boolean,
        val clubP0: Int,
    )

    data class Result(
        val overall: Int,
        val legacyN: Double,
    )

    fun apply(input: Input): Result {
        var legacyClubTier = input.clubO
        var declineWeight = (input.age - 31).toDouble()

        if (!input.clubR0) {
            legacyClubTier = when {
                input.clubP0 >= 4 -> 1
                input.clubP0 >= 3 -> 2
                else -> 3
            }
        }

        if (input.clubF0 >= 20) {
            declineWeight -= 2.0
        }

        declineWeight = when (input.overall) {
            in 1..50 -> declineWeight * 0.8
            in 51..70 -> declineWeight * 1.2
            in 71..100 -> declineWeight * 1.5
            else -> 0.0
        }

        var nextOverall = input.overall
        var nextN = input.legacyN

        if (declineWeight > 0.0) {
            val increment = declineWeight / 50.0
            val floor = when (legacyClubTier) {
                1 -> 35
                2 -> 25
                3 -> 10
                else -> 1
            }

            nextN += increment

            // Legacy uses strict `N > 1.0`, not >=, and preserves N when the floor blocks decline.
            if (nextN > 1.0 && nextOverall > floor) {
                nextOverall -= 1
                nextN -= 1.0
            }
        }

        // Final legacy clamp is unconditional.
        if (nextOverall < 1) {
            nextOverall = 1
        }

        return Result(
            overall = nextOverall,
            legacyN = nextN,
        )
    }
}
