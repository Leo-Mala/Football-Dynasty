package com.leomala.footballdynasty.migration.v1

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player
import com.leomala.footballdynasty.domain.model.RosterKind

object V1DomainAdapter {
    fun club(data: ClubDataV1): Club = Club(
        id = data.id,
        sourceFileRef = data.sourceFileRef,
        name = data.name,
        country = data.country,
        state = data.state,
        level = data.level,
        stadium = data.stadium,
        capacity = data.capacity,
        reputation = data.reputation,
        players = data.players.map(::player),
    )

    fun player(data: PlayerDataV1): Player = Player(
        id = data.id,
        clubId = data.sourceClubId,
        rosterKind = when (data.rosterKind) {
            RosterKindV1.SENIOR -> RosterKind.SENIOR
            RosterKindV1.JUNIOR -> RosterKind.JUNIOR
        },
        name = data.name,
        age = data.age,
        country = data.country,
        position = data.position,
        status = data.status,
        side = data.side,
        cr1 = data.cr1,
        cr2 = data.cr2,
        star = data.star,
        worldTop = data.worldTop,
    )
}
