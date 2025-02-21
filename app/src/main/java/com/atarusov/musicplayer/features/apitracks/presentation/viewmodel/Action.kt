package com.atarusov.musicplayer.features.apitracks.presentation.viewmodel

sealed class Action {
    data class SearchTrack(val query: String?) : Action()
    data object RepeatRequest : Action()
    data class ClickOnTrack(val trackId: Long) : Action()
}