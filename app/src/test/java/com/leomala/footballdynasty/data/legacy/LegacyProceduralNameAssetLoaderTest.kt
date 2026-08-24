package com.leomala.footballdynasty.data.legacy

import com.leomala.footballdynasty.foundation.random.RandomSource
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyProceduralNameAssetLoaderTest {
    @Test
    fun `country index resolves exact asset paths and legacy filter does not trim accepted lines`() {
        val opened = mutableListOf<String>()
        val assets = mapOf(
            "names/BRA.txt" to "\nAna\nA. B\nPlayer1\n Ana \nBia\n",
            "surnames/BRA.txt" to "Silva\nCosta\nSouza\n",
        )
        val loader = loader(assets, opened)

        val lists = requireNotNull(loader.load(29))

        assertEquals(listOf("names/BRA.txt", "surnames/BRA.txt"), opened)
        assertEquals(listOf("Ana", " Ana ", "Bia"), lists.names)
        assertEquals(listOf("Silva", "Costa", "Souza"), lists.surnames)
    }

    @Test
    fun `invalid legacy country has no fabricated fallback and opens no asset`() {
        val opened = mutableListOf<String>()
        val loader = loader(emptyMap(), opened)

        assertNull(loader.load(-1))
        assertNull(loader.load(221))
        assertEquals(emptyList<String>(), opened)
    }

    @Test
    fun `generate delegates deterministic name selection to legacy rules`() {
        val loader = loader(
            mapOf(
                "names/BRA.txt" to "IndexZero\nAna\nBia\n",
                "surnames/BRA.txt" to "IndexZero\nSilva\nCosta\n",
            ),
            mutableListOf(),
        )
        val random = QueueRandomSource(1, 2)

        assertEquals("Ana Costa", loader.generate(29, random))
        assertEquals(listOf(3, 3), random.bounds)
        assertEquals(2L, random.draws)
    }

    private fun loader(
        assets: Map<String, String>,
        opened: MutableList<String>,
    ) = LegacyProceduralNameAssetLoader { path ->
        opened += path
        ByteArrayInputStream(requireNotNull(assets[path]) { "Missing fake asset $path" }.toByteArray(Charsets.UTF_8))
    }

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val values = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = values.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
