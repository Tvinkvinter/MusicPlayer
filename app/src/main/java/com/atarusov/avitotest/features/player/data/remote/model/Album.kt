package com.atarusov.avitotest.features.player.data.remote.model

import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("cover_xl") val coverURL: String?,
    val title: String
)
