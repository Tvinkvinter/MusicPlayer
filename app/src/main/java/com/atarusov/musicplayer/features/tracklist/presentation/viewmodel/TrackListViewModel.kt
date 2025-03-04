package com.atarusov.musicplayer.features.tracklist.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atarusov.musicplayer.features.tracklist.presentation.SourceType
import com.atarusov.musicplayer.features.player.presentation.PlaylistByIds
import com.atarusov.musicplayer.features.tracklist.di.LocalRepository
import com.atarusov.musicplayer.features.tracklist.di.RemoteRepository
import com.atarusov.musicplayer.features.tracklist.domain.TrackRepository
import com.atarusov.musicplayer.features.tracklist.domain.model.Track
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TrackListViewModel(
    private val sourceType: SourceType,
    private val repository: TrackRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state
    private val _navigateToPlayer = MutableSharedFlow<PlaylistByIds>()
    val navigateToPlayer: SharedFlow<PlaylistByIds> = _navigateToPlayer

    // not null if error occurred during searching with some query
    private var errorQuery: String? = null

    init {
        loadTracks()
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.ClickOnTrack -> navigateToPlayer(
                action.trackId,
                _state.value.tracks.map { it.id })

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

    private fun navigateToPlayer(trackId: Long, playlistIds: List<Long>) {
        viewModelScope.launch {
            _navigateToPlayer.emit(
                PlaylistByIds(
                    currentTrackId = trackId,
                    allTrackIds = playlistIds,
                    type = sourceType
                )
            )
        }
    }

    private fun loadTracks() {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllTracks().catch { error ->
                showError(error)
            }.collect { tracks ->
                showTracks(tracks)
            }
        }
    }

    private fun searchTrack(query: String) {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTracksByQuery(query).catch { error ->
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
            isErrorShowing = false,
            noTracks = false
        )
    }

    private fun showError(error: Throwable) {
        Log.e("ApiTrackViewModel", error.message, error)
        _state.value = _state.value.copy(
            isLoading = false,
            isErrorShowing = true
        )
    }

    private fun showTracks(tracks: List<Track>) {
        errorQuery = null
        _state.value = _state.value.copy(
            tracks = tracks,
            isLoading = false,
            noTracks = tracks.isEmpty()
        )
    }

    @Suppress("UNCHECKED_CAST")
    class ViewModelFactory @AssistedInject constructor(
        @Assisted val sourceType: SourceType,
        @RemoteRepository private val remoteRepository: TrackRepository,
        @LocalRepository private val localRepository: TrackRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == TrackListViewModel::class.java) {
                return when(sourceType) {
                    SourceType.Remote -> TrackListViewModel(sourceType, remoteRepository) as T
                    SourceType.Local -> TrackListViewModel(sourceType, localRepository) as T
                }
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        @AssistedFactory
        interface FactoryProvider {
            fun create(sourceType: SourceType): ViewModelFactory
        }
    }
}