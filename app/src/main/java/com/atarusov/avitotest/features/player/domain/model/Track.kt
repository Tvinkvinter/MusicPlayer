package com.atarusov.avitotest.features.player.domain.model

data class Track(
    val id: Long,
    val coverURI: String?,
    val trackTitle: String?,
    val albumName: String?,
    val artistName: String,
    val trackURI: String
)