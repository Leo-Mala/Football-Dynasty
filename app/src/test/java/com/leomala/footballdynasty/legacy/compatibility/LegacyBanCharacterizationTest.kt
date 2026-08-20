package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.Base64

class LegacyBanCharacterizationTest {
    @Test
    fun `reads untouched legacy ban fixture`() {
        val encoded = requireNotNull(javaClass.getResourceAsStream("/legacy/12deoctubre_par.ban.b64"))
            .bufferedReader().use { it.readText() }
        val bytes = Base64.getMimeDecoder().decode(encoded)

        assertEquals(
            "7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7",
            sha256(bytes),
        )

        val team = LegacySerialization.readBan(ByteArrayInputStream(bytes))

        assertEquals("12 de Octubre", team.name)
        assertEquals("12deoctubre_par", team.fileRef)
        assertEquals(150, team.country)
        assertEquals(20, team.players.size)
        assertTrue(team.juniors.isEmpty())
        assertEquals("Mauro Cardozo", team.players.first().name)
        assertEquals(38, team.players.first().age)
        assertEquals(0, team.players.first().position)
        assertEquals(
            "9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6",
            DeterministicFingerprint.team(team),
        )
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
}
