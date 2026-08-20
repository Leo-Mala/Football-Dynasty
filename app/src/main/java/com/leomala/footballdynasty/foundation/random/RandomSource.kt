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
