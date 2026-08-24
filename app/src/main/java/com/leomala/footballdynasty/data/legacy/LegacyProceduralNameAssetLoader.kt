package com.leomala.footballdynasty.data.legacy

import android.content.res.AssetManager
import com.leomala.footballdynasty.domain.career.LegacyCountryAssetCodes
import com.leomala.footballdynasty.domain.career.LegacyProceduralNameRules
import com.leomala.footballdynasty.foundation.random.RandomSource
import java.io.InputStream

/**
 * Asset boundary for the official Brasfoot 2026/27 procedural name corpus.
 *
 * The loader intentionally has no generic/fabricated fallback. For every valid legacy country id it
 * opens the exact `names/<CODE>.txt` and `surnames/<CODE>.txt` paths recovered from the APK. Missing
 * assets are I/O failures so an incomplete factual corpus cannot silently change generated players.
 */
class LegacyProceduralNameAssetLoader internal constructor(
    private val openAsset: (String) -> InputStream,
) {
    constructor(assetManager: AssetManager) : this({ path -> assetManager.open(path) })

    data class NameLists(
        val names: List<String>,
        val surnames: List<String>,
    )

    fun load(legacyCountry: Int): NameLists? {
        val code = LegacyCountryAssetCodes.codeForLegacyCountry(legacyCountry) ?: return null
        return NameLists(
            names = readLegacyLines("names/$code.txt"),
            surnames = readLegacyLines("surnames/$code.txt"),
        )
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

    private fun readLegacyLines(path: String): List<String> =
        openAsset(path).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter(::isAcceptedLegacyLine).toList()
        }

    companion object {
        private val HAS_DIGIT = Regex(".*\\d+.*")

        /** Exact best.u.b() filter: no trim/normalization of accepted lines. */
        internal fun isAcceptedLegacyLine(line: String): Boolean =
            line.isNotEmpty() && !line.contains('.') && !HAS_DIGIT.matches(line)
    }
}
