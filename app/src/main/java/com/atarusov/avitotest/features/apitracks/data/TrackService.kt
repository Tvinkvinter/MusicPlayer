package com.atarusov.avitotest.features.apitracks.data

import com.atarusov.avitotest.features.apitracks.data.model.Chart
import com.atarusov.avitotest.features.apitracks.data.model.TrackList
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackService {

    @GET("/chart")
    suspend fun getChart(): Chart

    @GET("/search")
    suspend fun searchTrack(@Query("q") query: String): TrackList
}