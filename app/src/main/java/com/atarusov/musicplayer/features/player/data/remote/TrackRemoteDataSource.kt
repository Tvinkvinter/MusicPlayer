package com.atarusov.musicplayer.features.player.data.remote

import com.atarusov.musicplayer.features.player.data.remote.model.ApiTrackResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TrackRemoteDataSource @Inject constructor(private val service: TrackService) {
    fun getTrackFromApiById(id: Long): Flow<ApiTrackResponse> {
        return flow {
            emit(service.getTrackById(id))
        }
    }
}