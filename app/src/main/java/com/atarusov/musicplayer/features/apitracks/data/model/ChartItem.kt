package com.atarusov.musicplayer.features.apitracks.data.model

import com.atarusov.musicplayer.features.apitracks.domain.model.Track
import com.google.gson.annotations.SerializedName

data class ChartItem(
    val id: Long,
    @SerializedName("title") val trackTitle: String?,
    val album: Album,
    val artist: Artist,
) {
    fun toTrack(): Track = Track(
        id = id,
        coverURL = album.coverURL,
        trackTitle = trackTitle,
        artistName = artist.name,
    )
}
