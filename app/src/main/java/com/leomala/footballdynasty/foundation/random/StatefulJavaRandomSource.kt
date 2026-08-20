package com.leomala.footballdynasty.foundation.random

/**
 * Restorable implementation of java.util.Random's 48-bit LCG. This is deterministic
 * infrastructure; it does not claim that the legacy game used one global RNG instance.
 */
class StatefulJavaRandomSource private constructor(
    val initialSeed: Long,
    private var internalState: Long,
    override var draws: Long,
) : RandomSource {

    constructor(seed: Long) : this(
        initialSeed = seed,
        internalState = scramble(seed),
        draws = 0L,
    )

    override fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        val result = if ((bound and -bound) == bound) {
            ((bound.toLong() * next(31).toLong()) shr 31).toInt()
        } else {
            var bits: Int
            var value: Int
            do {
                bits = next(31)
                value = bits % bound
            } while (bits - value + (bound - 1) < 0)
            value
        }
        draws++
        return result
    }

    override fun nextBoolean(): Boolean = (next(1) != 0).also { draws++ }

    override fun nextDouble(): Double {
        val value = ((next(26).toLong() shl 27) + next(27).toLong()) / (1L shl 53).toDouble()
        draws++
        return value
    }

    fun snapshot(): StatefulRandomSnapshot = StatefulRandomSnapshot(
        initialSeed = initialSeed,
        internalState = internalState,
        draws = draws,
    )

    private fun next(bits: Int): Int {
        internalState = (internalState * MULTIPLIER + ADDEND) and MASK
        return (internalState ushr (48 - bits)).toInt()
    }

    companion object {
        const val MULTIPLIER: Long = 0x5DEECE66DL
        const val ADDEND: Long = 0xBL
        const val MASK: Long = (1L shl 48) - 1

        fun restore(snapshot: StatefulRandomSnapshot): StatefulJavaRandomSource {
            require(snapshot.internalState in 0L..MASK) { "Invalid Java Random internal state" }
            require(snapshot.draws >= 0L) { "Invalid draw count" }
            return StatefulJavaRandomSource(
                initialSeed = snapshot.initialSeed,
                internalState = snapshot.internalState,
                draws = snapshot.draws,
            )
        }

        private fun scramble(seed: Long): Long = (seed xor MULTIPLIER) and MASK
    }
}

data class StatefulRandomSnapshot(
    val initialSeed: Long,
    val internalState: Long,
    val draws: Long,
)
