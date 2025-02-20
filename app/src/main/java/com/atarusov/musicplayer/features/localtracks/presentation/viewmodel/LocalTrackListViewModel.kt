package com.atarusov.musicplayer.features.localtracks.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atarusov.musicplayer.features.localtracks.domain.TrackRepository
import com.atarusov.musicplayer.features.localtracks.domain.model.Track
import com.atarusov.musicplayer.features.player.domain.model.SourceType
import com.atarusov.musicplayer.features.player.presentation.PlaylistByIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocalTrackListViewModel(
    private val repository: TrackRepository
) : ViewModel() {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect

    // not null if error occurred during searching with some query
    private var errorQuery: String? = null

    fun onAction(action: Action) {
        when (action) {
            is Action.ReplyToPermissionRequest -> {
                if (action.isGranted) loadTracks()
                else showPermissionDenied()
            }

            is Action.ClickOnTrack -> sendEffect(Effect.NavigateToPlayer(
                PlaylistByIds(
                    currentTrackId = action.trackId,
                    allTrackIds = _state.value.tracks.map { it.id },
                    type = SourceType.Local,
                )
            ))

            Action.RepeatRequest -> {
                errorQuery?.let {
                    searchTrack(it)
                } ?: loadTracks()
            }

            is Action.SearchTrack -> {
                if (action.query.isNullOrEmpty()) loadTracks()
                else searchTrack(action.query)
            }
        }
    }

    private fun loadTracks() {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllLocalTracks().catch { error ->
                showError(error)
            }.collect { tracks ->
                showTracks(tracks)
            }
        }
    }

    private fun showPermissionDenied() {
        _state.value = _state.value.copy(
            tracks = emptyList(),
            isLoading = false,
            isPermissionDenied = true,
            isErrorShowing = false,
            noTracks = false
        )
    }

    private fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    private fun searchTrack(query: String) {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            repository.searchTrack(query).catch { error ->
                errorQuery = query
                showError(error)
            }.collect { tracks ->
                showTracks(tracks)
            }
        }
    }

    private fun showLoading() {
        _state.value = _state.value.copy(
            tracks = emptyList(),
            isLoading = true,
            isPermissionDenied = false,
            isErrorShowing = false,
            noTracks = false
        )
    }

    private fun showError(error: Throwable) {
        Log.e("LocalTrackViewModel", error.message, error)
        _state.value = _state.value.copy(
            tracks = emptyList(),
            isLoading = false,
            isPermissionDenied = false,
            isErrorShowing = true,
            noTracks = false
        )
    }

    private fun showTracks(tracks: List<Track>) {
        errorQuery = null
        _state.value = _state.value.copy(
            tracks = tracks,
            isLoading = false,
            isPermissionDenied = false,
            isErrorShowing = false,
            noTracks = tracks.isEmpty()
        )
    }

    class Factory @Inject constructor(
        private val repository: TrackRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == LocalTrackListViewModel::class.java) {
                return LocalTrackListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}