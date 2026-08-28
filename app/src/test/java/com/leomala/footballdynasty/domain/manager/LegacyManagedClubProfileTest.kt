package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyManagedClubProfileTest {
    @Test
    fun `projects only source backed club administration values`() {
        val club = Club(
            id = "club-a",
            sourceFileRef = "fixture.ban",
            name = "Fixture Club",
            country = -7,
            state = 0,
            level = 3,
            stadium = "Legacy Stadium",
            capacity = 54321,
            reputation = 87,
            players = emptyList(),
        )

        assertEquals(
            LegacyManagedClubProfile(
                clubId = "club-a",
                country = -7,
                state = 0,
                level = 3,
                stadium = "Legacy Stadium",
                capacity = 54321,
                reputation = 87,
            ),
            LegacyManagedClubProfileProjection.from(club),
        )
    }
}
