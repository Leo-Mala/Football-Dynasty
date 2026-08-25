package com.leomala.footballdynasty.data.legacy

import android.content.res.AssetManager
import com.leomala.footballdynasty.domain.career.LegacyCountryAssetCodes
import com.leomala.footballdynasty.domain.career.LegacyProceduralNameRules
import com.leomala.footballdynasty.foundation.random.RandomSource
import java.io.IOException
import java.io.InputStream

/**
 * Asset boundary for the byte-preserved Brasfoot 2026/27 procedural name corpus.
 *
 * The official legacy layout is materialized directly as `names/<CODE>.txt` and
 * `surnames/<CODE>.txt`. There is deliberately no generic/fabricated fallback.
 */
class LegacyProceduralNameAssetLoader internal constructor(
    private val openAsset: (String) -> InputStream,
) {
    constructor(assetManager: AssetManager) : this({ path -> assetManager.open(path) })

    data class NameLists(
        val names: List<String>,
        val surnames: List<String>,
    )

    private val cache = mutableMapOf<Int, NameLists>()

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
        return NameLists(
            names = readOfficialAsset(namesPath),
            surnames = readOfficialAsset(surnamesPath),
        )
    }

    private fun readOfficialAsset(path: String): List<String> {
        val accepted = try {
            openAsset(path).use { input ->
                // InputStreamReader with UTF-8 uses replacement for malformed input, matching
                // Android's legacy reader boundary (notably the single malformed byte in CRN).
                input.bufferedReader(Charsets.UTF_8).useLines { lines ->
                    lines.filter(::isAcceptedLegacyLine).toList()
                }
            }
        } catch (error: IOException) {
            throw IOException("Missing official procedural-name asset: $path", error)
        }
        if (accepted.isEmpty()) {
            throw IOException("Official procedural-name asset is empty after legacy filter: $path")
        }
        return accepted
    }

    companion object {
        private val HAS_DIGIT = Regex(".*\\d+.*")

        /** Exact best.u.b() filter: no trim/normalization of accepted lines. */
        internal fun isAcceptedLegacyLine(line: String): Boolean =
            line.isNotEmpty() && !line.contains('.') && !HAS_DIGIT.matches(line)
    }
}
