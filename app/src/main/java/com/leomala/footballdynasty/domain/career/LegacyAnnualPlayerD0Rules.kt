package com.leomala.footballdynasty.domain.career

/**
 * Pure projection of reachable legacy `best.o.d1(0)` / `best.o.D0()` annual state changes.
 *
 * The executable SMALI proves that `d1(int)` is a direct setter for `j0`. The annual `best.b.F()`
 * pass calls `d1(0)` for every global player before the later first-entry `D0()` pass. `D0()` then
 * increments `j0` and may latch the Boolean field `d` exposed by `W0()`/`M1(Boolean)`.
 *
 * This is deliberately kept under legacy-oriented names because the sporting meaning of the
 * counter/latch is not required to preserve the proven control flow.
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

    /** Exact projection of the annual `best.o.d1(0)` call. */
    fun resetGlobalCounter(input: Input): Input = input.copy(legacyJ0 = 0)

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
