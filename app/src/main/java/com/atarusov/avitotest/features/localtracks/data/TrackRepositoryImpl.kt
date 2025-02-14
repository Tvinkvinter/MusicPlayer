package com.atarusov.avitotest.features.localtracks.data

import com.atarusov.avitotest.features.localtracks.domain.TrackRepository
import com.atarusov.avitotest.features.localtracks.domain.model.Track
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(private val localDataSource: TrackLocalDataSource) :
    TrackRepository {
    override fun getAllLocalTracks(): Flow<List<Track>> {
        return localDataSource.getLocalTracks()
    }

    override fun searchTrack(query: String): Flow<List<Track>> {
        return localDataSource.getLocalTracks(query)
    }
}