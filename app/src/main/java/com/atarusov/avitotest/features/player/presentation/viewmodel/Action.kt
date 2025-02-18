package com.atarusov.avitotest.features.player.presentation.viewmodel

import com.atarusov.avitotest.features.player.presentation.PlaylistByIds

sealed class Action {
    data object PlayPause: Action()
    data object Next: Action()
    data object Prev: Action()
    data class Seek(val time: Int): Action()
    data object RewindBack: Action()
    data object RewindForward: Action()
    data class SetPlaylist(val playlist: PlaylistByIds): Action()
    data object CloseFragment: Action()
}