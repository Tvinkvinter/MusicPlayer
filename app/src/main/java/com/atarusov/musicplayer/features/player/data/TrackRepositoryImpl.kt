package com.atarusov.musicplayer.features.player.data

import com.atarusov.musicplayer.features.player.data.local.TrackLocalDataSource
import com.atarusov.musicplayer.features.player.data.remote.TrackRemoteDataSource
import com.atarusov.musicplayer.features.player.domain.TrackRepository
import com.atarusov.musicplayer.features.player.domain.model.SourceType
import com.atarusov.musicplayer.features.player.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    private val localDataSource: TrackLocalDataSource,
    private val remoteDataSource: TrackRemoteDataSource
) : TrackRepository {
    override fun getTrackById(id: Long, sourceType: SourceType): Flow<Track> {
        return if (sourceType == SourceType.Local) localDataSource.getLocalTrackById(id)
        else remoteDataSource.getTrackFromApiById(id).map { it.toTrack() }
    }
}