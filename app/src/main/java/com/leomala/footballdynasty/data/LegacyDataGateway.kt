package com.leomala.footballdynasty.data

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import com.leomala.footballdynasty.legacy.compatibility.LegacySerialization
import java.io.InputStream

class LegacyDataGateway {
    fun readTeamBan(input: InputStream): LegacyTeamSnapshot = LegacySerialization.readBan(input)
}
