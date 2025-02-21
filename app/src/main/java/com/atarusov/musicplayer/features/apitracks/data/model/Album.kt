package com.atarusov.musicplayer.features.apitracks.data.model

import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("cover_medium") val coverURL: String?
)
