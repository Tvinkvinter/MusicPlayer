package com.atarusov.avitotest.features.apitracks.domain

import com.atarusov.avitotest.features.apitracks.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun getChart(): Flow<List<Track>>
    fun searchTrack(query: String): Flow<List<Track>>
}