package com.leomala.footballdynasty.data.legacy

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.domain.career.LegacyProceduralPlayerRules
import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LegacyProceduralNameCorpusIntegrationTest {
    @Test
    fun `official corpus loads real Brazil lists and drives full annual procedural path`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val loader = LegacyProceduralNameAssetLoader(context.assets)
        val lists = requireNotNull(loader.load(29))

        assertEquals(1731, lists.names.size)
        assertEquals(684, lists.surnames.size)

        val random = ZeroRandomSource()
        val runtime = LegacyProceduralPlayerAssetRuntime(context.assets)
        val draft = runtime.generateAnnualDraft(
            random = random,
            target = LegacyProceduralPlayerRules.TargetContext(
                legacyF0 = 17,
                legacyP0 = 1,
                legacyR0 = false,
                legacyO = 0,
                legacyD = 29,
                requestedLegacyE = 3,
            ),
        )

        assertEquals(29, draft.legacyD)
        assertEquals("Adrianinho Silva", draft.name)
        assertEquals(15L, random.draws)
    }

    @Test
    fun `official corpus preserves malformed utf8 replacement semantics used by runtime`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val loader = LegacyProceduralNameAssetLoader(context.assets)
        val lists = requireNotNull(loader.load(48))

        assertTrue(lists.names.contains("X�"))
    }

    private class ZeroRandomSource : RandomSource {
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            require(bound > 0)
            draws++
            return 0
        }

        override fun nextBoolean(): Boolean = false.also { draws++ }
        override fun nextDouble(): Double = 0.0.also { draws++ }
    }
}
