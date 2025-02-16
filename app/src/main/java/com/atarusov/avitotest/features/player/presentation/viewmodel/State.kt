package com.atarusov.avitotest.features.player.presentation.viewmodel

import com.atarusov.avitotest.features.player.domain.model.Track

data class State(
    val track: Track? = null,
    val timeElapsed: Int = 0,
    val timeTotal: Int = 0,
    val isPlaying: Boolean = true,
    val isPrevButtonEnabled: Boolean = true,
    val isNextButtonEnabled: Boolean = true,
)
