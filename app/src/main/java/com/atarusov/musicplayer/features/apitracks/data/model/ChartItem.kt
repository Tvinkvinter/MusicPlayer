package com.atarusov.musicplayer.features.apitracks.data.model

import com.atarusov.musicplayer.features.apitracks.domain.model.Track

data class ChartItem(
    val id: Long,
    val title: String?,
    val album: Album,
    val artist: Artist,
) {
    fun toTrack(): Track = Track(
        id = id,
        coverURL = album.coverURL,
        trackTitle = title,
        artistName = artist.name,
    )
}
