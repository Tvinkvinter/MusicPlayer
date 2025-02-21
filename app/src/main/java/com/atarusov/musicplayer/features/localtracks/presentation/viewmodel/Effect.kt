package com.atarusov.musicplayer.features.localtracks.presentation.viewmodel

import com.atarusov.musicplayer.features.player.presentation.PlaylistByIds

sealed class Effect {
    data object RequestPermission: Effect()
    data class NavigateToPlayer(val playlist: PlaylistByIds): Effect()
}