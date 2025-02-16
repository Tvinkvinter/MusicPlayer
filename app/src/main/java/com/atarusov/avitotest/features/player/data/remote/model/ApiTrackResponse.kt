package com.atarusov.avitotest.features.player.data.remote.model

import com.atarusov.avitotest.features.player.domain.model.Track
import com.google.gson.annotations.SerializedName

data class ApiTrackResponse(
    val id: Long,
    val title: String?,
    val album: Album,
    val artist: Artist,
    @SerializedName("preview") val trackURL: String
) {
    fun toTrack(): Track = Track(
        id = id,
        coverURI = album.coverURL,
        trackTitle = title,
        albumName = album.title,
        artistName = artist.name,
        trackURI = trackURL
    )
}
