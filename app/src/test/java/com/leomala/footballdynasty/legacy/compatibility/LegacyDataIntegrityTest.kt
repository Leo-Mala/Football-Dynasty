package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.testsupport.LegacyFixtureLoader
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.security.MessageDigest

class LegacyDataIntegrityTest {
    @Test
    fun `fixture bytes and semantic snapshot remain frozen`() {
        val bytes = LegacyFixtureLoader.bytes("/legacy/12deoctubre_par.ban.b64", javaClass)
        assertEquals(
            "7f386a66e3e87042695b6dfaf23f2bc53143cfe8fa35b91a95ccd5ad060e85a7",
            sha256(bytes),
        )

        val team = LegacySerialization.readBan(ByteArrayInputStream(bytes))
        assertEquals(
            "9b0d1878744ce2d64a99db8a4103ba18e8f0286706ec4e30142cd585011d79a6",
            DeterministicFingerprint.team(team),
        )
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }
}
