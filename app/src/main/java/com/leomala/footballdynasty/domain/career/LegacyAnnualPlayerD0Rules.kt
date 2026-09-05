package com.leomala.footballdynasty.domain.career

/**
 * Pure projection of reachable legacy `best.o.D0()`.
 *
 * The executable SMALI first increments `j0`, then may latch the Boolean field `d` exposed by
 * `W0()`/`M1(Boolean)`. This is deliberately kept under legacy-oriented names because the sporting
 * meaning of that counter/latch is not required to preserve the proven control flow.
 */
object LegacyAnnualPlayerD0Rules {
    data class Input(
        val legacyJ0: Int,
        val legacyW0: Boolean,
        val hasClub: Boolean,
        val age: Int,
        val clubJ: Int = 0,
        val clubJ0: Int = 0,
    )

    data class Result(
        val legacyJ0: Int,
        val legacyW0: Boolean,
    )

    fun apply(input: Input): Result {
        // `add-int/2addr` has normal JVM 32-bit wraparound semantics.
        val nextJ0 = input.legacyJ0 + 1

        if (input.legacyW0 || nextJ0 < 2 || !input.hasClub || input.age >= 35) {
            return Result(
                legacyJ0 = nextJ0,
                legacyW0 = input.legacyW0,
            )
        }

        val shouldLatch = if (input.clubJ == 0) {
            when (input.clubJ0) {
                1, 65, 97 -> true
                104, 72, 154 -> nextJ0 >= 3
                else -> false
            }
        } else {
            input.clubJ0 == 29 && nextJ0 >= 4
        }

        return Result(
            legacyJ0 = nextJ0,
            legacyW0 = input.legacyW0 || shouldLatch,
        )
    }
}
