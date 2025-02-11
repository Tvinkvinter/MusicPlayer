package com.atarusov.avitotest.features.apitracks.domain.model

data class Track(
    val id: Int,
    val coverURL: String,
    val trackTitle: String,
    val albumTitle: String?,
    val artistName: String,
)
