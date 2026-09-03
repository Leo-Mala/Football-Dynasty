package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyCoachProfileTest {
    @Test
    fun `projects serialized coach identity`() {
        val team = LegacyTeamSnapshot(
            name = "Fixture Club",
            fileRef = "fixture.ban",
            country = 1,
            state = 2,
            level = 3,
            stadium = "Legacy Stadium",
            capacity = 40000,
            reputation = 75,
            players = emptyList(),
            juniors = emptyList(),
            coach = "Legacy Coach",
            coachCountry = 27,
        )

        val result = LegacyCoachProfileProjection.from(team)

        assertEquals("fixture.ban", result.teamFileRef)
        assertEquals("Legacy Coach", result.coachName)
        assertEquals(27, result.coachCountry)
    }

    @Test
    fun `pins only proven coach source fields`() {
        assertEquals(
            linkedSetOf("coach", "coachCountry"),
            LegacyCoachProfileProjection.provenSourceFields,
        )
    }

    @Test
    fun `preserves opaque coach values without normalization or club fallback`() {
        val team = LegacyTeamSnapshot(
            name = "Fixture Club",
            fileRef = "  fixture.ban  ",
            country = 91,
            state = 2,
            level = 3,
            stadium = "Legacy Stadium",
            capacity = 40000,
            reputation = 99,
            players = emptyList(),
            juniors = emptyList(),
            coach = "  Legacy Coach  ",
            coachCountry = Int.MIN_VALUE,
        )

        val result = LegacyCoachProfileProjection.from(team)

        assertEquals("  fixture.ban  ", result.teamFileRef)
        assertEquals("  Legacy Coach  ", result.coachName)
        assertEquals(Int.MIN_VALUE, result.coachCountry)
    }
}
