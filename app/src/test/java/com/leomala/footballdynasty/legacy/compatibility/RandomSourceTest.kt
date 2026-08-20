package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.foundation.random.SeededRandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class RandomSourceTest {
    @Test
    fun sameSeedGivesSameCharacterizationSequence() {
        val a = SeededRandomSource(270L)
        val b = SeededRandomSource(270L)
        val seqA = List(8) { a.nextInt(1000) }
        val seqB = List(8) { b.nextInt(1000) }
        assertEquals(seqA, seqB)
        assertEquals(8L, a.draws)
    }
}
