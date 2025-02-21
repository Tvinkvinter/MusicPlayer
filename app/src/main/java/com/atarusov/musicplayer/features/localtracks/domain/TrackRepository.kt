package com.atarusov.musicplayer.features.localtracks.domain

import com.atarusov.musicplayer.features.localtracks.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getAllLocalTracks(): Flow<List<Track>>
    fun searchTrack(query: String): Flow<List<Track>>
}