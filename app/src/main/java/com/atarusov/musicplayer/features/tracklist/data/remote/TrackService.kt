package com.atarusov.musicplayer.features.tracklist.data.remote

import com.atarusov.musicplayer.features.tracklist.data.remote.model.Chart
import com.atarusov.musicplayer.features.tracklist.data.remote.model.TrackList
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackService {

    @GET("/chart")
    suspend fun getChart(): Chart

    @GET("/search")
    suspend fun searchTrack(@Query("q") query: String): TrackList
}