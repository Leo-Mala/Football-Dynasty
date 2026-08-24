package com.leomala.footballdynasty.data.legacy

import android.content.res.AssetManager
import com.leomala.footballdynasty.domain.career.LegacyProceduralPlayerRules
import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Android/data adapter that closes the Phase 7 factual boundary: the pure procedural rules receive
 * names only from the official byte-preserved Brasfoot corpus.
 */
class LegacyProceduralPlayerAssetRuntime private constructor(
    private val nameLoader: LegacyProceduralNameAssetLoader,
) {
    constructor(assetManager: AssetManager) : this(LegacyProceduralNameAssetLoader(assetManager))

    internal constructor(openCorpus: () -> java.io.InputStream) :
        this(LegacyProceduralNameAssetLoader(openCorpus))

    fun generateAnnualDraft(
        random: RandomSource,
        target: LegacyProceduralPlayerRules.TargetContext,
    ): LegacyProceduralPlayerRules.Draft =
        LegacyProceduralPlayerRules.generateAnnualDraft(
            random = random,
            target = target,
            resolveName = { legacyCountry, source ->
                requireNotNull(nameLoader.generate(legacyCountry, source)) {
                    "No official procedural name corpus mapping for legacy country $legacyCountry"
                }
            },
        )
}
