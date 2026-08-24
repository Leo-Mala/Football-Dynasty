package com.leomala.footballdynasty.data.legacy

import com.leomala.footballdynasty.foundation.random.RandomSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyProceduralNameAssetLoaderTest {
    @Test
    fun `country index resolves exact virtual paths and legacy filter does not trim accepted lines`() {
        val loader = loader(
            mapOf(
                "names/BRA.txt" to "\nAna\nA. B\nPlayer1\n Ana \nBia\n",
                "surnames/BRA.txt" to "Silva\nCosta\nSouza\n",
            )
        )

        val lists = requireNotNull(loader.load(29))

        assertEquals(listOf("Ana", " Ana ", "Bia"), lists.names)
        assertEquals(listOf("Silva", "Costa", "Souza"), lists.surnames)
    }

    @Test
    fun `invalid legacy country has no fabricated fallback and does not open corpus`() {
        var opens = 0
        val loader = LegacyProceduralNameAssetLoader {
            opens++
            ByteArrayInputStream(ByteArray(0))
        }

        assertNull(loader.load(-1))
        assertNull(loader.load(221))
        assertEquals(0, opens)
    }

    @Test
    fun `generate delegates deterministic name selection to legacy rules`() {
        val loader = loader(
            mapOf(
                "names/BRA.txt" to "IndexZero\nAna\nBia\n",
                "surnames/BRA.txt" to "IndexZero\nSilva\nCosta\n",
            )
        )
        val random = QueueRandomSource(1, 2)

        assertEquals("Ana Costa", loader.generate(29, random))
        assertEquals(listOf(3, 3), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `valid legacy country fails hard when an official virtual path is absent`() {
        val loader = loader(mapOf("names/BRA.txt" to "Ana\nBia\n"))

        try {
            loader.load(29)
            throw AssertionError("Expected missing official surname asset to fail")
        } catch (expected: java.io.IOException) {
            assertEquals(
                "Missing official procedural-name asset: surnames/BRA.txt",
                expected.message,
            )
        }
    }

    private fun loader(entries: Map<String, String>): LegacyProceduralNameAssetLoader {
        val corpus = ByteArrayOutputStream().use { bytes ->
            ZipOutputStream(bytes).use { zip ->
                entries.forEach { (path, content) ->
                    zip.putNextEntry(ZipEntry(path))
                    zip.write(content.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
            }
            bytes.toByteArray()
        }
        return LegacyProceduralNameAssetLoader { ByteArrayInputStream(corpus) }
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
