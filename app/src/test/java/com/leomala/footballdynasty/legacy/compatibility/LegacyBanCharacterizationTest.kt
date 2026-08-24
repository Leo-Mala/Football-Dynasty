package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.Base64

class LegacyBanCharacterizationTest {
    @Test
    fun `reads untouched Brasfoot 2026 baseline ban fixture`() {
        val encoded = requireNotNull(javaClass.getResourceAsStream("/legacy/trepenne_smr.ban.b64"))
            .bufferedReader().use { it.readText() }
        val bytes = Base64.getMimeDecoder().decode(encoded)

        assertEquals(
            "c664cc841b44e39423835795b00bb1c248862eb0a0e1c579831857d748fa9281",
            sha256(bytes),
        )

        val team = LegacySerialization.readBan(ByteArrayInputStream(bytes))

        assertEquals("Tre Penne", team.name)
        assertEquals("trepenne_smr", team.fileRef)
        assertEquals(164, team.country)
        assertEquals(14, team.players.size)
        assertTrue(team.juniors.isEmpty())
        assertEquals("Mattia Migani", team.players.first().name)
        assertEquals(34, team.players.first().age)
        assertEquals(0, team.players.first().position)
        assertEquals(
            "edf33fe98fa1c490d4b86f2b73e172ecb6ba063e09fb6f289bec66fca2ca46c8",
            DeterministicFingerprint.team(team),
        )
    }

    @Test
    fun `keeps superseded fixture readable as historical regression only`() {
        val encoded = requireNotNull(javaClass.getResourceAsStream("/legacy/12deoctubre_par.ban.b64"))
            .bufferedReader().use { it.readText() }
        val bytes = Base64.getMimeDecoder().decode(encoded)

        assertEquals(
            "7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7",
            sha256(bytes),
        )
        assertEquals("12 de Octubre", LegacySerialization.readBan(ByteArrayInputStream(bytes)).name)
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
}
