package com.atarusov.avitotest.features.player.domain

import com.atarusov.avitotest.features.player.domain.model.Track
import com.atarusov.avitotest.features.player.domain.model.SourceType
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getTrackById(id: Long, sourceType: SourceType): Flow<Track>
}