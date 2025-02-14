package com.atarusov.avitotest.features.localtracks.presentation.viewmodel

sealed class Effect {
    data object RequestPermission: Effect()
    data class NavigateToPlayer(val trackId: Long): Effect()
}