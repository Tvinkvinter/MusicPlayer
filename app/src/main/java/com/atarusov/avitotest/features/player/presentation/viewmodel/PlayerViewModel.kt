package com.atarusov.avitotest.features.player.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atarusov.avitotest.features.player.domain.TrackRepository
import com.atarusov.avitotest.features.player.domain.model.SourceType
import com.atarusov.avitotest.features.player.presentation.PlaylistByIds
import com.atarusov.avitotest.features.player.presentation.service.NotificationAction
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

    private val _serviceCommand = MutableSharedFlow<ServiceCommand>(replay = 1)
    val serviceCommand: SharedFlow<ServiceCommand> = _serviceCommand

    private var isServiceBound = false

    private var trackIds = emptyList<Long>()
    private var currentTrackIdIndex = 0
    private var sourceType = SourceType.Local

    fun bindService(playerService: PlayerService) {
        if (isServiceBound) return
        startPlaybackTimeRetrieving(playerService)
        startNotificationActionRetrieving(playerService)
        isServiceBound = true
    }

    private fun startPlaybackTimeRetrieving(playerService: PlayerService) {
        viewModelScope.launch {
            playerService.playbackTime.collectLatest { playbackState ->
                playbackState?.let {
                    if (!state.value.isPlaying) return@let
                    val timeElapsed = (playbackState.timeElapsed / 1000).toInt()
                    val timeTotal = (max(0, playbackState.timeTotal) / 1000).toInt()
                    _state.value = _state.value.copy(
                        timeElapsed = timeElapsed,
                        timeTotal = max(0, timeTotal),
                        isPlaying = timeElapsed <= timeTotal
                    )
                }
            }
        }
    }

    private fun startNotificationActionRetrieving(playerService: PlayerService){
        viewModelScope.launch {
            playerService.notificationActions.collectLatest { notificationAction ->
                when (notificationAction) {
                    NotificationAction.PLAY_PAUSE -> onPlayPause()
                    NotificationAction.PREV -> onPrev()
                    NotificationAction.NEXT -> onNext()
                    NotificationAction.REWIND_BACK -> onRewindBack()
                    NotificationAction.REWIND_FORWARD -> onRewindForward()
                }
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.PlayPause -> onPlayPause()
            Action.Next -> onNext()
            Action.Prev -> onPrev()
            is Action.SetPlaylist -> onSetPlaylist(action.playlist)
            is Action.Seek -> onSeek(action.time)
            Action.RewindBack -> onRewindBack()
            Action.RewindForward -> onRewindForward()
            Action.CloseFragment -> sendCommand(ServiceCommand.StopService)
        }
    }

    private fun sendCommand(serviceCommand: ServiceCommand) {
        viewModelScope.launch {
            _serviceCommand.emit(serviceCommand)
        }
    }

    private fun onPlayPause() {
        if (state.value.isPlaying) {
            sendCommand(ServiceCommand.Pause)
            _state.value = _state.value.copy(isPlaying = false)
        } else {
            sendCommand(ServiceCommand.Play)
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
                    sendCommand(ServiceCommand.SetTrackAndPlay(track))
                    _state.value = _state.value.copy(track = track, isPlaying = true)
                }
        }
    }

    private fun onSeek(time: Int) {
        sendCommand(ServiceCommand.Seek(time * 1000L))
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