package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyManagedClubSourceSquadsTest {
    @Test
    fun preservesExactSourceRefAndSeparateCollectionOrder() {
        val team = team(
            players = listOf(player("Senior A", 101), player("Senior B", 102)),
            juniors = listOf(player("Junior A", 201), player("Junior B", 202)),
        )

        val result = LegacyManagedClubSourceSquadProjection.from(team)

        assertEquals("teams/exact.ban", result.sourceFileRef)
        assertEquals(listOf("Senior A", "Senior B"), result.senior.map { it.name })
        assertEquals(listOf(101, 102), result.senior.map { it.legacyHash })
        assertEquals(listOf("Junior A", "Junior B"), result.juniors.map { it.name })
        assertEquals(listOf(201, 202), result.juniors.map { it.legacyHash })
    }

    @Test
    fun keepsSeniorAndJuniorCollectionsDisjoint() {
        val team = team(
            players = listOf(player("Senior", 301)),
            juniors = listOf(player("Junior", 401)),
        )

        val result = LegacyManagedClubSourceSquadProjection.from(team)

        assertEquals(listOf("Senior"), result.senior.map { it.name })
        assertEquals(listOf("Junior"), result.juniors.map { it.name })
    }

    @Test
    fun createsExactFileCollectionAndIndexReferencesWithoutFactMatching() {
        val result = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Same", 501), player("Same", 502)),
                juniors = listOf(player("Same", 601)),
            ),
        )

        assertEquals(
            listOf(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 0),
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 1),
            ),
            result.seniorRefs(),
        )
        assertEquals(
            listOf(LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.JUNIOR, 0)),
            result.juniorRefs(),
        )
        assertEquals(502, result.seniorPlayer(result.seniorRefs()[1])?.legacyHash)
        assertEquals(601, result.juniorPlayer(result.juniorRefs()[0])?.legacyHash)
    }

    @Test
    fun createsSingleReferencesOnlyForExistingCollectionIndexes() {
        val result = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Senior A", 610), player("Senior B", 611)),
                juniors = listOf(player("Junior A", 612)),
            ),
        )

        assertEquals(
            LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 1),
            result.seniorRef(1),
        )
        assertEquals(
            LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.JUNIOR, 0),
            result.juniorRef(0),
        )
        assertNull(result.seniorRef(-1))
        assertNull(result.seniorRef(2))
        assertNull(result.juniorRef(-1))
        assertNull(result.juniorRef(1))
    }

    @Test
    fun filtersOpaqueSeniorCodesWithoutChangingSourceIdentityOrOrder() {
        val result = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(
                    player("A", 620, position = 9, status = 4, side = 7),
                    player("B", 621, position = 2, status = 4, side = 3),
                    player("C", 622, position = 9, status = 8, side = 7),
                ),
                juniors = listOf(
                    player("Junior same codes", 623, position = 9, status = 4, side = 7),
                ),
            ),
        )

        assertEquals(
            listOf(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 0),
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 2),
            ),
            result.seniorRefsByPositionCode(9),
        )
        assertEquals(
            listOf(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 0),
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 1),
            ),
            result.seniorRefsByStatusCode(4),
        )
        assertEquals(
            listOf(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 0),
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 2),
            ),
            result.seniorRefsBySideCode(7),
        )
        assertEquals(emptyList<LegacySourcePlayerRef>(), result.seniorRefsByPositionCode(99))
        assertEquals(emptyList<LegacySourcePlayerRef>(), result.seniorRefsByStatusCode(99))
        assertEquals(emptyList<LegacySourcePlayerRef>(), result.seniorRefsBySideCode(99))
    }

    @Test
    fun rawIdentityLookupReturnsEveryExactSeniorMatchInSourceOrderOnly() {
        val result = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(
                    player("First exact", 700),
                    player("Different", 701),
                    player("Second exact", 700),
                ),
                juniors = listOf(player("Junior exact", 700)),
            ),
        )

        val identity = LegacyRawPlayerIdentity(
            legacyAid = 7,
            legacySid = 8,
            legacyTid = 9,
            legacyHash = 700,
        )

        assertEquals(
            listOf(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 0),
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 2),
            ),
            result.seniorRefsByRawIdentity(identity),
        )
        assertEquals(
            emptyList<LegacySourcePlayerRef>(),
            result.seniorRefsByRawIdentity(identity.copy(legacyTid = 99)),
        )
    }

    @Test
    fun rawIdentityLookupReturnsEveryExactJuniorMatchWithoutCrossingIntoSeniorRoster() {
        val result = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Senior exact", 710)),
                juniors = listOf(
                    player("Junior first exact", 710),
                    player("Junior different", 711),
                    player("Junior second exact", 710),
                ),
            ),
        )

        val identity = LegacyRawPlayerIdentity(
            legacyAid = 7,
            legacySid = 8,
            legacyTid = 9,
            legacyHash = 710,
        )

        assertEquals(
            listOf(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.JUNIOR, 0),
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.JUNIOR, 2),
            ),
            result.juniorRefsByRawIdentity(identity),
        )
        assertEquals(
            listOf(LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 0)),
            result.seniorRefsByRawIdentity(identity),
        )
    }

    @Test
    fun rawIdentityIsResolvedOnlyThroughExactFileCollectionAndIndexProvenance() {
        val result = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Senior", 720)),
                juniors = listOf(player("Junior", 721)),
            ),
        )

        assertEquals(
            LegacyRawPlayerIdentity(legacyAid = 7, legacySid = 8, legacyTid = 9, legacyHash = 720),
            result.rawIdentity(result.seniorRefs().single()),
        )
        assertEquals(
            LegacyRawPlayerIdentity(legacyAid = 7, legacySid = 8, legacyTid = 9, legacyHash = 721),
            result.rawIdentity(result.juniorRefs().single()),
        )
        assertNull(
            result.rawIdentity(
                LegacySourcePlayerRef("teams/other.ban", LegacySourceRosterKind.SENIOR, 0),
            ),
        )
        assertNull(
            result.rawIdentity(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 1),
            ),
        )
    }

    @Test
    fun neverFallsBackAcrossFilesCollectionsOrOutsideExactSourceIndex() {
        val result = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Shared", 701)),
                juniors = listOf(player("Shared", 801)),
            ),
        )

        assertNull(
            result.seniorPlayer(
                LegacySourcePlayerRef("teams/other.ban", LegacySourceRosterKind.SENIOR, 0),
            ),
        )
        assertNull(
            result.juniorPlayer(
                LegacySourcePlayerRef("teams/other.ban", LegacySourceRosterKind.JUNIOR, 0),
            ),
        )
        assertNull(
            result.seniorPlayer(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.JUNIOR, 0),
            ),
        )
        assertNull(
            result.juniorPlayer(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 0),
            ),
        )
        assertNull(
            result.seniorPlayer(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.SENIOR, 1),
            ),
        )
        assertNull(
            result.juniorPlayer(
                LegacySourcePlayerRef("teams/exact.ban", LegacySourceRosterKind.JUNIOR, 1),
            ),
        )
    }

    @Test
    fun identicalRosterIndexCannotResolveAgainstAnotherTeamSource() {
        val exact = LegacyManagedClubSourceSquadProjection.from(
            team(
                fileRef = "teams/exact.ban",
                players = listOf(player("Exact Senior", 901)),
                juniors = listOf(player("Exact Junior", 902)),
            ),
        )
        val other = LegacyManagedClubSourceSquadProjection.from(
            team(
                fileRef = "teams/other.ban",
                players = listOf(player("Other Senior", 903)),
                juniors = listOf(player("Other Junior", 904)),
            ),
        )

        assertNull(other.seniorPlayer(exact.seniorRefs().single()))
        assertNull(other.juniorPlayer(exact.juniorRefs().single()))
        assertEquals(901, exact.seniorPlayer(exact.seniorRefs().single())?.legacyHash)
        assertEquals(902, exact.juniorPlayer(exact.juniorRefs().single())?.legacyHash)
    }

    private fun player(
        name: String,
        legacyHash: Int,
        position: Int = 2,
        status: Int = 3,
        side: Int = 4,
    ): LegacyPlayerSnapshot = LegacyPlayerSnapshot(
        name = name,
        age = 18,
        country = 1,
        position = position,
        status = status,
        side = side,
        cr1 = 5,
        cr2 = 6,
        star = false,
        worldTop = false,
        legacyAid = 7,
        legacySid = 8,
        legacyTid = 9,
        legacyHash = legacyHash,
    )

    private fun team(
        players: List<LegacyPlayerSnapshot>,
        juniors: List<LegacyPlayerSnapshot>,
        fileRef: String = "teams/exact.ban",
    ): LegacyTeamSnapshot = LegacyTeamSnapshot(
        name = "Exact",
        fileRef = fileRef,
        country = 1,
        state = 2,
        level = 3,
        stadium = "Stadium",
        capacity = 10000,
        reputation = 4,
        players = players,
        juniors = juniors,
    )
}
