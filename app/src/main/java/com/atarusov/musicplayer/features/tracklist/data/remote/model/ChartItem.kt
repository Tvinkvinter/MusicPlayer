package com.atarusov.musicplayer.features.tracklist.data.remote.model

import com.atarusov.musicplayer.features.tracklist.domain.model.Track

data class ChartItem(
    val id: Long,
    val title: String?,
    val album: Album,
    val artist: Artist,
) {
    fun toTrack(): Track = Track(
        id = id,
        coverURI = album.coverURL,
        trackTitle = title,
        artistName = artist.name,
    )
}
