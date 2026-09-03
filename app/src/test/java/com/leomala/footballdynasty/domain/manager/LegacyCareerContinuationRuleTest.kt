package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerContinuationRuleTest {
    private class Manager(val id: String, val human: Boolean)
    private class Match(val id: String)

    @Test
    fun mainTeamPathShortCircuitsRuntimeAndStillClearsR3WhenF2IsTrue() {
        val calls = mutableListOf<String>()
        val result = LegacyCareerContinuationRule.execute(
            setS2 = { calls += "S2:$it" }, setSpecialFlag = { calls += "a:$it" },
            s1 = { calls += "S1"; true }, p0 = { calls += "p0"; Any() },
            h1 = { error("short-circuit H1") }, i1 = { error("short-circuit i1") },
            legacyIF = { error("short-circuit i.f") }, e2 = { error("short-circuit e2") },
            setQ3 = { error("must not Q3") }, g1 = { error("short-circuit G1") },
            legacyIS = { error("short-circuit i.s") }, runY3G = { error("must not y3.g") },
            runSR0 = { error("must not s.R0") }, legacyV0 = { error("must not V0") },
            legacyIH = { error("must not i.h") }, runII = { error("must not i.i") },
            openMainTeam = { calls += "ActivityMainTeam" }, dispatchPendingMatch = { error("must not h") },
            dispatchPostSeason = { error("must not j") }, f2 = { calls += "f2"; true },
            setR3 = { calls += "R3:$it" },
        )
        assertFalse(result.specialFlag)
        assertEquals(listOf("S2:false", "a:false", "S1", "p0", "ActivityMainTeam", "f2", "R3:false"), calls)
        assertEquals(listOf(LegacyCareerContinuationEffect.SET_S2_FALSE, LegacyCareerContinuationEffect.RESET_SPECIAL_FLAG, LegacyCareerContinuationEffect.OPEN_MAIN_TEAM, LegacyCareerContinuationEffect.SET_R3_FALSE), result.effectsInOrder)
    }

    @Test
    fun h1SpecialGateReadsE2TwiceThenForcesPostSeasonWithoutReadingIH() {
        val calls = mutableListOf<String>()
        var e2Reads = 0
        val result = LegacyCareerContinuationRule.execute(
            setS2 = { calls += "S2:$it" }, setSpecialFlag = { calls += "a:$it" },
            s1 = { calls += "S1"; false }, p0 = { error("S1 false") },
            h1 = { calls += "H1"; true }, i1 = { calls += "i1"; 1 },
            legacyIF = { calls += "i.f"; false }, e2 = { calls += "e2:${++e2Reads}"; e2Reads == 2 },
            setQ3 = { calls += "Q3:$it" }, g1 = { calls += "G1"; false }, legacyIS = { error("G1 false") },
            runY3G = { calls += "y3.g" }, runSR0 = { calls += "s.R0" }, legacyV0 = { calls += "V0"; true },
            legacyIH = { error("special flag short-circuits i.h") }, runII = { error("must not i.i") },
            openMainTeam = { error("must not main") }, dispatchPendingMatch = { error("must not h") },
            dispatchPostSeason = { calls += "j:$it" }, f2 = { calls += "f2"; false }, setR3 = { error("f2 false") },
        )
        assertTrue(result.specialFlag)
        assertEquals(2, e2Reads)
        assertEquals(listOf("S2:false", "a:false", "S1", "H1", "i1", "i.f", "e2:1", "a:true", "e2:2", "Q3:false", "G1", "y3.g", "s.R0", "V0", "j:true", "f2"), calls)
    }

    @Test
    fun cleanV0PathRunsIIThenPendingMatchDispatcher() {
        val calls = mutableListOf<String>()
        val result = LegacyCareerContinuationRule.execute(
            setS2 = { calls += "S2:$it" }, setSpecialFlag = { calls += "a:$it" }, s1 = { false }, p0 = { error("short-circuit") },
            h1 = { false }, i1 = { error("H1/G1 false") }, legacyIF = { error("H1 false") }, e2 = { false }, setQ3 = { error("e2 false") },
            g1 = { false }, legacyIS = { error("G1 false") }, runY3G = { calls += "y3.g" }, runSR0 = { calls += "s.R0" },
            legacyV0 = { calls += "V0"; true }, legacyIH = { calls += "i.h"; true }, runII = { calls += "i.i" }, openMainTeam = {},
            dispatchPendingMatch = { calls += "h:$it" }, dispatchPostSeason = { error("must choose h") }, f2 = { false }, setR3 = {},
        )
        assertFalse(result.specialFlag)
        assertEquals(listOf("S2:false", "a:false", "y3.g", "s.R0", "V0", "i.h", "i.i", "h:false"), calls)
    }

    @Test
    fun negativePendingMatchIndexClearsExistingBufferAndFallsThroughToJ() {
        val calls = mutableListOf<String>()
        val result = LegacyPendingMatchLaunchRule.execute<Match>(
            pendingMatchIndex = { calls += "i.a"; -1 }, hasActivityMatchBuffer = { calls += "B?"; true }, clearActivityMatchBuffer = { calls += "B=null" },
            setActivityF = { error("negative") }, setActivityG = { error("negative") }, setActivityI = { error("negative") }, createActivityMatchBuffer = { error("negative") },
            pendingMatches = { error("negative") }, markMatch = { _, _ -> error("negative") }, openMatch = { error("negative") }, dispatchPostSeason = { calls += "j" },
        )
        assertEquals(-1, result.pendingMatchIndex)
        assertEquals(listOf("i.a", "B?", "B=null", "j"), calls)
    }

    @Test
    fun positivePendingMatchIndexInitializesActivityStateMarksMatchAndOpensExactIndex() {
        val calls = mutableListOf<String>()
        val first = Match("0")
        val second = Match("1")
        val result = LegacyPendingMatchLaunchRule.execute(
            pendingMatchIndex = { calls += "i.a"; 1 }, hasActivityMatchBuffer = { error("positive") }, clearActivityMatchBuffer = { error("positive") },
            setActivityF = { calls += "F:$it" }, setActivityG = { calls += "G:$it" }, setActivityI = { calls += "I:$it" }, createActivityMatchBuffer = { calls += "B=new" },
            pendingMatches = { calls += "i.b"; listOf(first, second) }, markMatch = { match, value -> calls += "d:${match.id}:$value" },
            openMatch = { calls += "ActivityJogo:$it" }, dispatchPostSeason = { error("positive") },
        )
        assertEquals(1, result.pendingMatchIndex)
        assertEquals(listOf("i.a", "F:0", "G:1", "I:false", "B=new", "i.b", "d:1:false", "ActivityJogo:1"), calls)
    }

    @Test
    fun postSeasonHostRunsWorldMutationsBeforeResultsDecision() {
        val calls = mutableListOf<String>()
        val effects = LegacyPostSeasonResultsRule.execute(
            runSG = { calls += "s.g" }, runBV = { calls += "b.v" }, runWorldE = { calls += "E" }, runWorldM = { calls += "m" },
            resetNationalInvites = { calls += "g=null" }, setJ2 = { calls += "j2:$it" }, legacyV0 = { calls += "V0"; true }, specialFlag = false,
            openResults = { calls += "ActivityResults" }, dispatchInvitations = { error("must open results") },
        )
        assertEquals(listOf("s.g", "b.v", "E", "m", "g=null", "j2:0", "V0", "ActivityResults"), calls)
        assertTrue(effects.contains(LegacyPostSeasonResultsEffect.OPEN_RESULTS))
    }

    @Test
    fun nationalInvitesTakePriorityAndManagerScanRepeatsH0ExactlyLikeSmali() {
        val calls = mutableListOf<String>()
        val ai = Manager("ai", false)
        val human = Manager("human", true)
        var selectedState: Manager? = Manager("sentinel", false)
        var clubState: List<*>? = listOf("sentinel")
        var h0Reads = 0
        val result = LegacyInvitationDispatchRule.execute(
            setClubInvites = { calls += "f=${it?.size}"; clubState = it }, setSelectedManager = { calls += "i=${it?.id}"; selectedState = it },
            currentCompetitionKind = { calls += "kind"; 2 }, q0 = { calls += "Q0"; 0 }, loadClubInvites = { kind, zero -> calls += "f4:$kind:$zero"; listOf("club") },
            nationalInvites = { calls += "g"; listOf("national") }, managers = { calls += "H0:${++h0Reads}"; listOf(ai, human) },
            isHumanManager = { calls += "K:${it.id}"; it.human }, openNationalInvitation = { calls += "ActivityConviteSelecao:${it?.id}" },
            openClubInvitation = { error("national priority") }, dispatchDismissals = { error("national priority") },
        )
        assertEquals(5, h0Reads)
        assertSame(human, result.selectedManager)
        assertSame(human, selectedState)
        assertEquals(listOf("club"), clubState)
        assertTrue(result.effectsInOrder.contains(LegacyInvitationDispatchEffect.OPEN_NATIONAL_INVITATION))
    }

    @Test
    fun kindThreeWithNonzeroQ0LoadsFalseClubInvitesAndOpensClubInvitation() {
        val human = Manager("human", true)
        val calls = mutableListOf<String>()
        val result = LegacyInvitationDispatchRule.execute(
            setClubInvites = { calls += "f=${it?.size}" }, setSelectedManager = { calls += "i=${it?.id}" }, currentCompetitionKind = { 3 }, q0 = { 9 },
            loadClubInvites = { kind, zero -> calls += "f4:$kind:$zero"; listOf("offer") }, nationalInvites = { emptyList<Any>() }, managers = { listOf(human) },
            isHumanManager = { it.human }, openNationalInvitation = { error("no national") }, openClubInvitation = { calls += "ActivityConvite:${it?.id}" },
            dispatchDismissals = { error("club invite exists") },
        )
        assertSame(human, result.selectedManager)
        assertTrue(result.effectsInOrder.contains(LegacyInvitationDispatchEffect.LOAD_CLUB_INVITES_FALSE))
        assertTrue(calls.contains("f4:3:false"))
    }

    @Test
    fun noInvitationListsFallThroughToDismissalGateWithoutManagerScan() {
        val calls = mutableListOf<String>()
        val result = LegacyInvitationDispatchRule.execute<Manager>(
            setClubInvites = { calls += "f=${it?.size}" }, setSelectedManager = { calls += "i=${it?.id}" }, currentCompetitionKind = { calls += "kind"; 2 },
            q0 = { calls += "Q0"; 4 }, loadClubInvites = { _, _ -> error("must not f4") }, nationalInvites = { calls += "g"; null },
            managers = { error("must not H0") }, isHumanManager = { error("must not K") }, openNationalInvitation = { error("no invite") },
            openClubInvitation = { error("no invite") }, dispatchDismissals = { calls += "l" },
        )
        assertNull(result.clubInvitations)
        assertNull(result.selectedManager)
        assertEquals(listOf("f=null", "i=null", "kind", "Q0", "g", "l"), calls)
    }
}
