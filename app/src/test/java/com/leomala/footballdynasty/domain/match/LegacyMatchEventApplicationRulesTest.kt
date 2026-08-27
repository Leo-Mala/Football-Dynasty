package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchEventApplicationRulesTest {
    private data class Club(val code: String)
    private data class Player(val code: String)

    private val home = Club("home")
    private val away = Club("away")
    private val primary = Player("primary")
    private val secondary = Player("secondary")

    @Test
    fun `home and away club identity map to legacy sides zero and one`() {
        assertEquals(0, resolve(eventClub = home).event.legacySide)
        assertEquals(1, resolve(eventClub = away).event.legacySide)
    }

    @Test
    fun `club resolution uses identity rather than value equality`() {
        val equalButDistinctHome = Club("home")
        val result = resolve(eventClub = equalButDistinctHome, type = 5, substitutions = 1)

        assertFalse(result.recognizedClubSide)
        assertEquals(0, result.event.legacySide)
        assertFalse(LegacyMatchEventApplicationRules.Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE in result.operations)
        // Injury's final legacy check uses non-null c0 + N[side] rather than active-list recognition.
        assertEquals(0, result.substitutionRequest?.side)
    }

    @Test
    fun `event fields preserve period minute club primary and secondary mapping`() {
        val result = resolve(type = 6, subtype = -1, period = 2, minute = 47)

        assertEquals(6, result.event.legacyType)
        assertEquals(-1, result.event.legacySubtype)
        assertSame(home, result.event.legacyClub)
        assertSame(primary, result.event.primaryPlayer)
        assertSame(secondary, result.event.secondaryPlayer)
        assertEquals(47, result.event.legacyMinute)
        assertEquals(2, result.event.legacyPeriod)
    }

    @Test
    fun `subtype two successful V selection overwrites only event primary`() {
        val oppositeSelected = Player("opposite")
        var calls = 0
        val result = resolve(
            type = 1,
            subtype = 2,
            selectOpposite = {
                calls++
                oppositeSelected
            },
        )

        assertEquals(1, calls)
        assertSame(oppositeSelected, result.event.primaryPlayer)
        assertEquals(2, result.event.legacySubtype)
        assertTrue(result.eventPrimaryWasReplacedByOppositeSelection)
    }

    @Test
    fun `subtype two failed V selection falls back to subtype one and original primary`() {
        var calls = 0
        val result = resolve(
            type = 1,
            subtype = 2,
            selectOpposite = {
                calls++
                null
            },
        )

        assertEquals(1, calls)
        assertSame(primary, result.event.primaryPlayer)
        assertEquals(1, result.event.legacySubtype)
        assertFalse(result.eventPrimaryWasReplacedByOppositeSelection)
    }

    @Test
    fun `yellow card appends event before legacy stat m`() {
        val result = resolve(type = 2)

        assertEquals(
            listOf(
                LegacyMatchEventApplicationRules.Operation.APPEND_EVENT,
                LegacyMatchEventApplicationRules.Operation.LEGACY_PLAYER_STAT_M,
            ),
            result.operations,
        )
    }

    @Test
    fun `direct red appends event then legacy stat n then removes active primary`() {
        val result = resolve(type = 4, position = 14)

        assertEquals(
            listOf(
                LegacyMatchEventApplicationRules.Operation.APPEND_EVENT,
                LegacyMatchEventApplicationRules.Operation.LEGACY_PLAYER_STAT_N,
                LegacyMatchEventApplicationRules.Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE,
            ),
            result.operations,
        )
        assertNull(result.substitutionRequest)
    }

    @Test
    fun `second yellow red increments m then n before removal and substitution request`() {
        val result = resolve(type = 3, position = 13, substitutions = 1)

        assertEquals(
            listOf(
                LegacyMatchEventApplicationRules.Operation.APPEND_EVENT,
                LegacyMatchEventApplicationRules.Operation.LEGACY_PLAYER_STAT_M,
                LegacyMatchEventApplicationRules.Operation.LEGACY_PLAYER_STAT_N,
                LegacyMatchEventApplicationRules.Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE,
                LegacyMatchEventApplicationRules.Operation.REQUEST_SUBSTITUTION,
            ),
            result.operations,
        )
        val request = result.substitutionRequest!!
        assertEquals(0, request.side)
        assertSame(primary, request.originalPlayer)
        assertTrue(request.automaticOutgoing)
        assertFalse(request.enforceLegacyL0Compatibility)
    }

    @Test
    fun `red substitution position threshold is inclusive at thirteen`() {
        assertTrue(resolve(type = 4, position = 13, substitutions = 1).substitutionRequest != null)
        assertNull(resolve(type = 4, position = 14, substitutions = 1).substitutionRequest)
    }

    @Test
    fun `red still removes primary when club mode flag blocks substitution`() {
        val result = resolve(type = 4, position = 1, substitutions = 5, clubMode = true)

        assertTrue(LegacyMatchEventApplicationRules.Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE in result.operations)
        assertNull(result.substitutionRequest)
    }

    @Test
    fun `red does not request substitution when remaining count is zero`() {
        val result = resolve(type = 4, position = 1, substitutions = 0)

        assertTrue(LegacyMatchEventApplicationRules.Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE in result.operations)
        assertNull(result.substitutionRequest)
    }

    @Test
    fun `injury applies injury before removal and primary substitution request`() {
        val result = resolve(type = 5, position = 8, substitutions = 2, period = 2, minute = 32)

        assertEquals(
            listOf(
                LegacyMatchEventApplicationRules.Operation.APPEND_EVENT,
                LegacyMatchEventApplicationRules.Operation.APPLY_INJURY_TO_ORIGINAL_PRIMARY,
                LegacyMatchEventApplicationRules.Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE,
                LegacyMatchEventApplicationRules.Operation.REQUEST_SUBSTITUTION,
            ),
            result.operations,
        )
        val request = result.substitutionRequest!!
        assertFalse(request.automaticOutgoing)
        assertTrue(request.enforceLegacyL0Compatibility)
        assertEquals(2, request.legacyPeriod)
        assertEquals(32, request.legacyMinute)
        assertSame(primary, request.originalPlayer)
    }

    @Test
    fun `injury club mode keeps injury effect but skips active removal and substitution`() {
        val result = resolve(type = 5, substitutions = 5, clubMode = true)

        assertEquals(
            listOf(
                LegacyMatchEventApplicationRules.Operation.APPEND_EVENT,
                LegacyMatchEventApplicationRules.Operation.APPLY_INJURY_TO_ORIGINAL_PRIMARY,
            ),
            result.operations,
        )
        assertNull(result.substitutionRequest)
    }

    @Test
    fun `injury with null primary skips injury call but preserves legacy remove and substitution routing`() {
        val result = resolve(type = 5, substitutions = 1, primaryPlayer = null)

        assertFalse(LegacyMatchEventApplicationRules.Operation.APPLY_INJURY_TO_ORIGINAL_PRIMARY in result.operations)
        assertTrue(LegacyMatchEventApplicationRules.Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE in result.operations)
        assertTrue(LegacyMatchEventApplicationRules.Operation.REQUEST_SUBSTITUTION in result.operations)
        assertNull(result.substitutionRequest?.originalPlayer)
    }

    @Test
    fun `injury on unrecognized non-null club preserves legacy side-zero substitution quirk without removal`() {
        val otherClub = Club("other")
        val result = resolve(eventClub = otherClub, type = 5, substitutions = 1)

        assertFalse(result.recognizedClubSide)
        assertFalse(LegacyMatchEventApplicationRules.Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE in result.operations)
        assertEquals(0, result.substitutionRequest?.side)
        assertFalse(result.substitutionRequest!!.automaticOutgoing)
        assertTrue(result.substitutionRequest!!.enforceLegacyL0Compatibility)
    }

    @Test
    fun `non disciplinary non injury event has only append operation`() {
        for (type in listOf(1, 6, 7, 99)) {
            assertEquals(
                "type=$type",
                listOf(LegacyMatchEventApplicationRules.Operation.APPEND_EVENT),
                resolve(type = type).operations,
            )
        }
    }

    @Test
    fun `executor preserves exact recovered operation order`() {
        val result = resolve(type = 3, position = 1, substitutions = 1)
        val seen = mutableListOf<String>()

        LegacyMatchEventApplicationRules.execute(
            result = result,
            appendEvent = { seen += "append:${it.legacyType}" },
            applyLegacyPlayerStatM = { seen += "m" },
            applyLegacyPlayerStatN = { seen += "n" },
            applyInjuryToOriginalPrimary = { seen += "injury" },
            removeOriginalPrimaryFromActive = { seen += "remove" },
            requestSubstitution = { seen += "p1:${it.automaticOutgoing}:${it.enforceLegacyL0Compatibility}" },
        )

        assertEquals(listOf("append:3", "m", "n", "remove", "p1:true:false"), seen)
    }

    @Test
    fun `subtype two event replacement does not change original player used by later red effects`() {
        val selected = Player("opposite")
        val result = resolve(
            type = 4,
            subtype = 2,
            position = 1,
            substitutions = 1,
            selectOpposite = { selected },
        )

        assertSame(selected, result.event.primaryPlayer)
        assertSame(primary, result.substitutionRequest!!.originalPlayer)
    }

    private fun resolve(
        type: Int = 1,
        subtype: Int = -1,
        eventClub: Club? = home,
        primaryPlayer: Player? = primary,
        position: Int = 8,
        substitutions: Int = 0,
        clubMode: Boolean = false,
        period: Int = 1,
        minute: Int = 12,
        selectOpposite: () -> Player? = { null },
    ) = LegacyMatchEventApplicationRules.resolve(
        legacyType = type,
        legacySubtype = subtype,
        homeClub = home,
        awayClub = away,
        eventClub = eventClub,
        originalPrimary = primaryPlayer,
        secondaryPlayer = secondary,
        originalPrimaryPositionIndex = position,
        legacyPeriod = period,
        legacyMinute = minute,
        substitutionsRemainingForResolvedSide = substitutions,
        legacyClubModeFlag = clubMode,
        selectLegacyVFromOppositeActive = selectOpposite,
    )
}
