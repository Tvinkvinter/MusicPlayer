package com.atarusov.avitotest.features.localtracks.presentation.viewmodel

import com.atarusov.avitotest.features.player.presentation.PlaylistByIds

sealed class Effect {
    data object RequestPermission: Effect()
    data class NavigateToPlayer(val playlist: PlaylistByIds): Effect()
}