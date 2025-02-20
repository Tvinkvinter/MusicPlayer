package com.atarusov.musicplayer.features.apitracks.data

import com.atarusov.musicplayer.features.apitracks.domain.TrackRepository
import com.atarusov.musicplayer.features.apitracks.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(private val remoteDataSource: TrackRemoteDataSource) :
    TrackRepository {

    override fun getChart(): Flow<List<Track>> {
        val apiResponseFlow = remoteDataSource.getChart()
        return apiResponseFlow.map { chart -> chart.tracks.data.map { it.toTrack() } }
    }

    override fun searchTrack(query: String): Flow<List<Track>> {
        val apiResponseFlow = remoteDataSource.searchTrack(query)
        return apiResponseFlow.map { tracks -> tracks.data.map { it.toTrack() } }
    }
}