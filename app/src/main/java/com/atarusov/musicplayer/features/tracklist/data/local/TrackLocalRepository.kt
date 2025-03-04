package com.atarusov.musicplayer.features.tracklist.data.local


import android.util.Log
import com.atarusov.musicplayer.features.tracklist.domain.TrackRepository
import com.atarusov.musicplayer.features.tracklist.domain.model.Track
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackLocalRepository @Inject constructor(private val localDataSource: TrackLocalDataSource) :
    TrackRepository {

    override fun getAllTracks(): Flow<List<Track>> {
        Log.i("TrackLocalRepository", "getAllTracks")
        return localDataSource.getLocalTracks()
    }

    override fun getTracksByQuery(query: String): Flow<List<Track>> {
        return localDataSource.getLocalTracks(query)
    }
}