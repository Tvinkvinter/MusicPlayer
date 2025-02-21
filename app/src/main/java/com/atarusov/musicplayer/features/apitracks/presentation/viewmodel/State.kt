package com.atarusov.musicplayer.features.apitracks.presentation.viewmodel

import com.atarusov.musicplayer.features.apitracks.domain.model.Track

data class State(
    val tracks: List<Track> = emptyList(),
    val noTracks: Boolean = true,
    val isLoading: Boolean = false,
    val isErrorShowing: Boolean = false,
)