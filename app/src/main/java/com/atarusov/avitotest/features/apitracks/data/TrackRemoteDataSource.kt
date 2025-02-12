package com.atarusov.avitotest.features.apitracks.data

import com.atarusov.avitotest.features.apitracks.data.model.Chart
import com.atarusov.avitotest.features.apitracks.data.model.TrackList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TrackRemoteDataSource @Inject constructor(private val service: TrackService) {

    fun getChart(): Flow<Chart> {
        return flow {
            emit(service.getChart())
        }
    }

    fun searchTrack(query: String): Flow<TrackList> {
        return flow {
            emit(service.searchTrack(query))
        }
    }
}