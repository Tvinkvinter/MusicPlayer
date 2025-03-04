package com.atarusov.musicplayer.features.tracklist.data.remote

import com.atarusov.musicplayer.features.tracklist.domain.TrackRepository
import com.atarusov.musicplayer.features.tracklist.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackRemoteRepository @Inject constructor(private val remoteDataSource: TrackRemoteDataSource) :
    TrackRepository {

    override fun getAllTracks(): Flow<List<Track>> {
        val apiResponseFlow = remoteDataSource.getChart()
        return apiResponseFlow.map { chart -> chart.tracks.data.map { it.toTrack() } }
    }

    override fun getTracksByQuery(query: String): Flow<List<Track>> {
        val apiResponseFlow = remoteDataSource.searchTrack(query)
        return apiResponseFlow.map { tracks -> tracks.data.map { it.toTrack() } }
    }
}