package com.leomala.footballdynasty.foundation.random

import java.util.Random

interface RandomSource {
    val draws: Long
    fun nextInt(bound: Int): Int
    fun nextBoolean(): Boolean
    fun nextDouble(): Double
}

class SeededRandomSource(seed: Long) : RandomSource {
    private val delegate = Random(seed)

    override var draws: Long = 0
        private set

    override fun nextInt(bound: Int): Int = delegate.nextInt(bound).also { draws++ }
    override fun nextBoolean(): Boolean = delegate.nextBoolean().also { draws++ }
    override fun nextDouble(): Double = delegate.nextDouble().also { draws++ }
}

/**
 * One fresh legacy-compatible `new java.util.Random()` source.
 *
 * Legacy `best.o.n(...)` creates a new Random for every player rating instead of consuming the
 * persisted career RNG. Callers that reproduce that boundary must create a new instance per call.
 */
class FreshJavaRandomSource : RandomSource {
    private val delegate = Random()

    override var draws: Long = 0
        private set

    override fun nextInt(bound: Int): Int = delegate.nextInt(bound).also { draws++ }
    override fun nextBoolean(): Boolean = delegate.nextBoolean().also { draws++ }
    override fun nextDouble(): Double = delegate.nextDouble().also { draws++ }
}
