package com.atarusov.musicplayer.features.tracklist.domain.model

data class Track(
    val id: Long,
    val coverURI: String?,
    val trackTitle: String?,
    val artistName: String,
)