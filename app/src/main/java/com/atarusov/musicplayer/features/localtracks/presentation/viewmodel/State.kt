package com.atarusov.musicplayer.features.localtracks.presentation.viewmodel

import com.atarusov.musicplayer.features.localtracks.domain.model.Track

data class State(
    val tracks: List<Track> = emptyList(),
    val isPermissionDenied: Boolean = true,
    val noTracks: Boolean = true,
    val isLoading: Boolean = false,
    val isErrorShowing: Boolean = false,
)