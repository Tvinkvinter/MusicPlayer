package com.atarusov.avitotest.features.player.presentation.viewmodel

import com.atarusov.avitotest.features.player.domain.model.Track

sealed class ServiceCommand {
    data class SetTrackAndPlay(val track: Track) : ServiceCommand()
    data object Play : ServiceCommand()
    data object Pause : ServiceCommand()
    data class Seek(val time: Long) : ServiceCommand()
    data object StopService: ServiceCommand()
}