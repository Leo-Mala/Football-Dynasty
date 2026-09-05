package com.leomala.footballdynasty.migration.v1

import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.FootballDynastyMigrations
import com.leomala.footballdynasty.data.local.V1RoomAdapter
import com.leomala.footballdynasty.foundation.error.ImportVersionException
import com.leomala.footballdynasty.foundation.error.UnsupportedLegacySaveException
import com.leomala.footballdynasty.legacy.compatibility.LegacySaveReader
import com.leomala.footballdynasty.legacy.compatibility.LegacySerialization
import com.leomala.footballdynasty.testsupport.LegacyFixtureLoader
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V1AdaptersIdentityTest {
    @Test
    fun `legacy fixture maps deterministically and preserves opaque fields`() {
        val snapshot = legacySnapshot()
        val adapter = LegacyBanToV1Adapter()

        val first = adapter.adapt(snapshot)
        val second = adapter.adapt(snapshot)

        assertEquals(first.id, second.id)
        assertEquals(first.players.map { it.id }, second.players.map { it.id })
        assertEquals(snapshot.fileRef, first.sourceFileRef)
        assertEquals(snapshot.level, first.level)
        assertEquals(snapshot.primaryColor, first.primaryColor)
        assertEquals(snapshot.secondaryColor, first.secondaryColor)
        assertEquals(snapshot.coach, first.coach)
        assertEquals(snapshot.coachCountry, first.coachCountry)
        assertEquals(snapshot.baseColor, first.baseColor)
        assertEquals(snapshot.legacyAid, first.legacyAid)
        assertEquals(snapshot.legacySid, first.legacySid)
        assertEquals(snapshot.legacyTid, first.legacyTid)
        assertEquals(snapshot.legacyVid, first.legacyVid)
        assertEquals(snapshot.legacyId, first.legacyId)
        assertEquals(snapshot.legacyValid, first.legacyValid)

        val sourcePlayer = snapshot.players.first()
        val mappedPlayer = first.players.first()
        assertEquals(sourcePlayer.legacyAid, mappedPlayer.legacyAid)
        assertEquals(sourcePlayer.legacySid, mappedPlayer.legacySid)
        assertEquals(sourcePlayer.legacyTid, mappedPlayer.legacyTid)
        assertEquals(sourcePlayer.legacyHash, mappedPlayer.legacyHash)
        assertEquals(sourcePlayer.name, mappedPlayer.name)
        assertEquals(sourcePlayer.age, mappedPlayer.age)
        assertEquals(sourcePlayer.country, mappedPlayer.country)
        assertEquals(sourcePlayer.position, mappedPlayer.position)
        assertEquals(sourcePlayer.status, mappedPlayer.status)
        assertEquals(sourcePlayer.side, mappedPlayer.side)
        assertEquals(sourcePlayer.cr1, mappedPlayer.cr1)
        assertEquals(sourcePlayer.cr2, mappedPlayer.cr2)
        assertEquals(sourcePlayer.star, mappedPlayer.star)
        assertEquals(sourcePlayer.worldTop, mappedPlayer.worldTop)
    }

    @Test
    fun `legacy team level reaches modern club unchanged through every V1 adapter`() {
        val snapshot = legacySnapshot()
        val data = LegacyBanToV1Adapter().adapt(snapshot)
        val domain = V1DomainAdapter.club(data)
        val entity = V1RoomAdapter.clubEntity(data, LEGACY_BAN_IMPORT_SCOPE)
        val roundTrip = V1RoomAdapter.clubData(entity, emptyList(), emptyList())

        assertEquals(snapshot.level, data.level)
        assertEquals(snapshot.level, domain.level)
        assertEquals(snapshot.level, entity.level)
        assertEquals(snapshot.level, roundTrip.level)
    }

    @Test
    fun `club V1 round trip through Room entities is lossless and row order independent`() {
        val data = LegacyBanToV1Adapter().adapt(legacySnapshot())
        val clubEntity = V1RoomAdapter.clubEntity(data, LEGACY_BAN_IMPORT_SCOPE)
        val players = data.players.map { V1RoomAdapter.playerEntity(it, LEGACY_BAN_IMPORT_SCOPE) }
        val memberships = data.players.map(V1RoomAdapter::membershipEntity)

        val roundTrip = V1RoomAdapter.clubData(clubEntity, players.reversed(), memberships)

        assertEquals(data, roundTrip)
        assertEquals(data.level, clubEntity.level)
        assertEquals(data.level, roundTrip.level)
        assertEquals(data.players.map { it.id }, roundTrip.players.map { it.id })
        assertEquals(V1Fingerprint.corpus(listOf(data)), V1Fingerprint.corpus(listOf(roundTrip)))
    }

    @Test
    fun `career V1 round trip preserves optional legacy fingerprints`() {
        val data = CareerDataV1(
            id = "career-technical-probe",
            displayName = "Technical career probe",
            legacyMetadataFingerprint = "metadata-fingerprint-probe",
            legacyCareerFingerprint = "career-fingerprint-probe",
        )
        val entity = V1RoomAdapter.careerEntity(data, createdAtEpochMillis = 10L, updatedAtEpochMillis = 20L)
        assertEquals(data, V1RoomAdapter.careerData(entity))
        assertEquals(data, V1DomainAdapter.careerData(V1DomainAdapter.career(data)))
    }

    @Test
    fun `unsupported V1 entity version fails explicitly`() {
        val data = LegacyBanToV1Adapter().adapt(legacySnapshot()).copy(schemaVersion = 99)
        val error = runCatching { V1RoomAdapter.clubEntity(data, LEGACY_BAN_IMPORT_SCOPE) }.exceptionOrNull()
        assertTrue(error is ImportVersionException)
    }

    @Test
    fun `database V14 preserves explicit ordered migration registry from V1`() {
        assertEquals(14, FootballDynastyDatabase.SCHEMA_VERSION)
        assertEquals(13, FootballDynastyMigrations.ALL.size)
        assertEquals(
            listOf(1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7, 7 to 8, 8 to 9, 9 to 10, 10 to 11, 11 to 12, 12 to 13, 13 to 14),
            FootballDynastyMigrations.ALL.map { it.startVersion to it.endVersion },
        )
    }

    @Test
    fun `full legacy career reader remains explicitly blocked without real fixture`() {
        val error = runCatching {
            LegacySaveReader().readCareer(ByteArrayInputStream(byteArrayOf(1, 2, 3)))
        }.exceptionOrNull()
        assertTrue(error is UnsupportedLegacySaveException)
    }

    private fun legacySnapshot() = LegacySerialization.readBan(
        ByteArrayInputStream(
            LegacyFixtureLoader.bytes("/legacy/12deoctubre_par.ban.b64", javaClass)
        )
    )
}
