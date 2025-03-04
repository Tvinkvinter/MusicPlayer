package com.atarusov.musicplayer.features.tracklist.data.remote.model

import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("cover_medium") val coverURL: String?
)
