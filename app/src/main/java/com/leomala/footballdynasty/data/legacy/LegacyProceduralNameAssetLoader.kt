package com.leomala.footballdynasty.data.legacy

import android.content.res.AssetManager
import com.leomala.footballdynasty.domain.career.LegacyCountryAssetCodes
import com.leomala.footballdynasty.domain.career.LegacyProceduralNameRules
import com.leomala.footballdynasty.foundation.random.RandomSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Asset boundary for the official Brasfoot 2026/27 procedural name corpus.
 *
 * Runtime ships the 442 byte-preserved legacy entries inside one deterministic ZIP split into
 * [CORPUS_PARTS] only to keep repository writes transport-safe. Concatenating the parts reproduces
 * the exact packaged ZIP; its virtual paths remain `names/<CODE>.txt` / `surnames/<CODE>.txt`.
 * There is deliberately no generic/fabricated fallback.
 */
class LegacyProceduralNameAssetLoader internal constructor(
    private val openCorpus: () -> InputStream,
) {
    constructor(assetManager: AssetManager) : this({ openBundledCorpus(assetManager) })

    data class NameLists(
        val names: List<String>,
        val surnames: List<String>,
    )

    private val cache = mutableMapOf<Int, NameLists>()
    private val corpusBytes: ByteArray by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        openCorpus().use { it.readBytes() }
    }

    @Synchronized
    fun load(legacyCountry: Int): NameLists? {
        val code = LegacyCountryAssetCodes.codeForLegacyCountry(legacyCountry) ?: return null
        return cache.getOrPut(legacyCountry) { loadCountry(code) }
    }

    /** Signature matches the resolver callback used by LegacyProceduralPlayerRules.generateAnnualDraft. */
    fun generate(legacyCountry: Int, random: RandomSource): String? {
        val lists = load(legacyCountry) ?: return null
        return LegacyProceduralNameRules.generate(
            random = random,
            names = lists.names,
            surnames = lists.surnames,
        )
    }

    private fun loadCountry(code: String): NameLists {
        val namesPath = "names/$code.txt"
        val surnamesPath = "surnames/$code.txt"
        var names: List<String>? = null
        var surnames: List<String>? = null

        ZipInputStream(ByteArrayInputStream(corpusBytes).buffered()).use { zip ->
            while (names == null || surnames == null) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    when (entry.name) {
                        namesPath -> names = readLegacyLines(zip.readBytes())
                        surnamesPath -> surnames = readLegacyLines(zip.readBytes())
                    }
                }
                zip.closeEntry()
            }
        }

        return NameLists(
            names = names ?: throw IOException("Missing official procedural-name asset: $namesPath"),
            surnames = surnames ?: throw IOException("Missing official procedural-name asset: $surnamesPath"),
        )
    }

    private fun readLegacyLines(bytes: ByteArray): List<String> =
        ByteArrayInputStream(bytes).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter(::isAcceptedLegacyLine).toList()
        }

    companion object {
        internal val CORPUS_PARTS = arrayOf(
            "legacy_name_corpus.zip.part00",
            "legacy_name_corpus.zip.part01",
            "legacy_name_corpus.zip.part02",
            "legacy_name_corpus.zip.part03",
            "legacy_name_corpus.zip.part04",
            "legacy_name_corpus.zip.part05",
            "legacy_name_corpus.zip.part06",
            "legacy_name_corpus.zip.part07",
        )
        private val HAS_DIGIT = Regex(".*\\d+.*")

        private fun openBundledCorpus(assetManager: AssetManager): InputStream {
            val bytes = ByteArrayOutputStream()
            CORPUS_PARTS.forEach { part ->
                assetManager.open(part).use { input -> input.copyTo(bytes) }
            }
            return ByteArrayInputStream(bytes.toByteArray())
        }

        /** Exact best.u.b() filter: no trim/normalization of accepted lines. */
        internal fun isAcceptedLegacyLine(line: String): Boolean =
            line.isNotEmpty() && !line.contains('.') && !HAS_DIGIT.matches(line)
    }
}
