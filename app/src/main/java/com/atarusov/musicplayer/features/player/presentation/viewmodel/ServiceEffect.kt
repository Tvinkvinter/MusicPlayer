package com.atarusov.musicplayer.features.player.presentation.viewmodel

import com.atarusov.musicplayer.features.player.domain.model.Track
import kotlinx.coroutines.flow.Flow

sealed class ServiceEffect {
    data class SetTrackAndPlay(val track: Track) : ServiceEffect()
    data object Play : ServiceEffect()
    data object Pause : ServiceEffect()
    data class Seek(val time: Long) : ServiceEffect()
    data object Init : ServiceEffect()

    // callback for retrieving flow with elapsed and total track time
    data class RequestTrackTimeFlow(
        val subscribeFunction: (flow: Flow<Pair<Long, Long>>) -> Unit
    ) : ServiceEffect()
}