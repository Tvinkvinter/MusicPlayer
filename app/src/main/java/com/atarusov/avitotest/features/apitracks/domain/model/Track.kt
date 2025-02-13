package com.atarusov.avitotest.features.apitracks.domain.model

data class Track(
    val id: Long,
    val coverURL: String?,
    val trackTitle: String?,
    val artistName: String,
)
