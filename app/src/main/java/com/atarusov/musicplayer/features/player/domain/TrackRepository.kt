package com.atarusov.musicplayer.features.player.domain

import com.atarusov.musicplayer.features.player.domain.model.Track
import com.atarusov.musicplayer.features.tracklist.presentation.SourceType
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getTrackById(id: Long, sourceType: SourceType): Flow<Track>
}