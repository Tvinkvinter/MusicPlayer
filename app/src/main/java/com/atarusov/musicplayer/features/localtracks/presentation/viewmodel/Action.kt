package com.atarusov.musicplayer.features.localtracks.presentation.viewmodel

sealed class Action {
    data class ReplyToPermissionRequest(val isGranted: Boolean): Action()
    data class SearchTrack(val query: String?) : Action()
    data object RepeatRequest : Action()
    data class ClickOnTrack(val trackId: Long) : Action()
}
