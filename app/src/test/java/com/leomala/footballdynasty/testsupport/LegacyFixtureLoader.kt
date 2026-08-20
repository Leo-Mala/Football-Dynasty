package com.leomala.footballdynasty.testsupport

import java.util.Base64

object LegacyFixtureLoader {
    fun bytes(resourcePath: String, owner: Class<*>): ByteArray {
        val encoded = requireNotNull(owner.getResourceAsStream(resourcePath)) {
            "Missing fixture: $resourcePath"
        }.bufferedReader().use { it.readText() }
        return Base64.getMimeDecoder().decode(encoded)
    }
}
