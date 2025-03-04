package com.atarusov.musicplayer.features.tracklist.domain

import com.atarusov.musicplayer.features.tracklist.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getAllTracks(): Flow<List<Track>>
    fun getTracksByQuery(query: String): Flow<List<Track>>
}