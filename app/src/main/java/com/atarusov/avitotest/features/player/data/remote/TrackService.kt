package com.atarusov.avitotest.features.player.data.remote

import com.atarusov.avitotest.features.player.data.remote.model.ApiTrackResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface TrackService {

    @GET("/track/{id}")
    suspend fun getTrackById(@Path("id") id: Long): ApiTrackResponse

}