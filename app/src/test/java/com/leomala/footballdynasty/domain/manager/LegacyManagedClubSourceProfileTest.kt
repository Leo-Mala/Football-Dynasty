package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyManagedClubSourceProfileTest {
    @Test
    fun `preserves exact legacy administrative source facts without interpretation`() {
        val team = LegacyTeamSnapshot(
            name = " Legacy Club ",
            fileRef = "teams/legacy-club.ban",
            country = -11,
            state = 22,
            level = 33,
            stadium = " Legacy Stadium ",
            capacity = -4444,
            reputation = 55,
            players = emptyList(),
            juniors = emptyList(),
        )

        assertEquals(
            LegacyManagedClubSourceProfile(
                sourceFileRef = "teams/legacy-club.ban",
                name = " Legacy Club ",
                country = -11,
                state = 22,
                level = 33,
                stadium = " Legacy Stadium ",
                capacity = -4444,
                reputation = 55,
            ),
            LegacyManagedClubSourceProfileProjection.from(team),
        )
    }
}
