package com.leomala.footballdynasty.domain.repository

import com.leomala.footballdynasty.domain.model.Club

interface ClubRepository {
    suspend fun findById(id: String): Club?
    suspend fun findBySourceFileRef(sourceFileRef: String): Club?
}
