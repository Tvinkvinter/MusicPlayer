package com.atarusov.avitotest.features.player.presentation.viewmodel

import com.atarusov.avitotest.features.player.domain.model.Track

sealed class ServiceEffect {
    data class SetTrackAndPlay(val track: Track) : ServiceEffect()
    data object Play : ServiceEffect()
    data object Pause : ServiceEffect()
    data class Seek(val time: Long) : ServiceEffect()
    data object Init : ServiceEffect()
}