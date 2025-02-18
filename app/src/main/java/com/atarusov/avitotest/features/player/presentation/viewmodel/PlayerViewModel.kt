package com.atarusov.avitotest.features.player.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atarusov.avitotest.features.player.domain.TrackRepository
import com.atarusov.avitotest.features.player.domain.model.SourceType
import com.atarusov.avitotest.features.player.domain.model.Track
import com.atarusov.avitotest.features.player.presentation.PlaylistByIds
import com.atarusov.avitotest.features.player.presentation.service.PlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class PlayerViewModel(
    private val repository: TrackRepository
) : ViewModel() {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _serviceEffect = MutableSharedFlow<ServiceEffect>()
    val serviceEffect: SharedFlow<ServiceEffect> = _serviceEffect

    private var isServiceConnected = false
    private var isSubscribedOnTrackTime = false

    private var trackIds = emptyList<Long>()
    private var currentTrackIdIndex = 0
    private var sourceType = SourceType.Local

    private var trackToSetOnServiceIsReady: Track? = null

    init {
        sendEffect(ServiceEffect.Init)
    }

    fun onAction(action: Action) {
        when (action) {
            Action.NotifyServiceConnected -> onServiceConnected()
            Action.PlayPause -> onPlayPause()
            Action.Next -> onNext()
            Action.Prev -> onPrev()
            is Action.SetPlaylist -> onSetPlaylist(action.playlist)
            is Action.Seek -> onSeek(action.time)
            Action.RewindBack -> onRewindBack()
            Action.RewindForward -> onRewindForward()
        }
    }


    private fun sendEffect(serviceEffect: ServiceEffect) {
        viewModelScope.launch {
            _serviceEffect.emit(serviceEffect)
        }
    }

    fun bindService(playerService: PlayerService) {
        if (isSubscribedOnTrackTime) return
        startPlaybackTimeRetrieving(playerService)
    }
    
    private fun startPlaybackTimeRetrieving(playerService: PlayerService) {
        viewModelScope.launch {
            playerService.playbackTime.collect { playbackState ->
                isSubscribedOnTrackTime = true
                playbackState?.let {
                    val timeElapsed = (playbackState.timeElapsed / 1000).toInt()
                    val timeTotal = (max(0, playbackState.timeTotal) / 1000).toInt()
                    _state.value = _state.value.copy(
                        timeElapsed = timeElapsed,
                        timeTotal = max(0, timeTotal),
                        isPlaying = timeElapsed < timeTotal
                    )
                }
            }
        }
    }

    private fun onServiceConnected() {
        isServiceConnected = true
        trackToSetOnServiceIsReady?.let {
            sendEffect(ServiceEffect.SetTrackAndPlay(it))
            trackToSetOnServiceIsReady = null
        }
    }

    private fun onPlayPause() {
        if (state.value.isPlaying) {
            sendEffect(ServiceEffect.Pause)
            _state.value = _state.value.copy(isPlaying = false)
        } else {
            sendEffect(ServiceEffect.Play)
            _state.value = _state.value.copy(isPlaying = true)
        }
    }

    private fun onPrev() {
        if (currentTrackIdIndex <= 0) return

        val nextTrackId = trackIds[--currentTrackIdIndex]
        _state.value = _state.value.copy(
            isNextButtonEnabled = true,
            isPrevButtonEnabled = currentTrackIdIndex >= 1
        )
        loadTrackById(nextTrackId)
    }

    private fun onNext() {
        if (currentTrackIdIndex >= trackIds.size - 1) return

        val nextTrackId = trackIds[++currentTrackIdIndex]
        _state.value = _state.value.copy(
            isNextButtonEnabled = currentTrackIdIndex < trackIds.size - 1,
            isPrevButtonEnabled = true
        )
        loadTrackById(nextTrackId)
    }

    private fun onSetPlaylist(playlist: PlaylistByIds) {
        if (playlist.allTrackIds == trackIds) return
        trackIds = playlist.allTrackIds
        currentTrackIdIndex = trackIds.indexOf(playlist.currentTrackId)
        sourceType = playlist.type
        loadTrackById(playlist.currentTrackId)
    }

    private fun loadTrackById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTrackById(id, sourceType)
                .catch { error ->
                    Log.e("PlayerViewModel", error.message, error)
                }
                .collectLatest { track ->
                    if (isServiceConnected) sendEffect(ServiceEffect.SetTrackAndPlay(track))
                    else trackToSetOnServiceIsReady = track
                    _state.value = _state.value.copy(track = track, isPlaying = true)
                }
        }
    }

    private fun onSeek(time: Int) {
        sendEffect(ServiceEffect.Seek(time * 1000L))
        _state.value = _state.value.copy(
            timeElapsed = time,
            isPlaying = true
        )
    }

    private fun onRewindBack() {
        val time = max(0, state.value.timeElapsed - 5)
        onSeek(time)
    }

    private fun onRewindForward() {
        val time = min(state.value.timeElapsed + 5, state.value.timeTotal)
        onSeek(time)
    }

    class Factory @Inject constructor(
        private val repository: TrackRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == PlayerViewModel::class.java) {
                return PlayerViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}