package com.atarusov.avitotest.features.apitracks.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atarusov.avitotest.features.apitracks.domain.TrackRepository
import com.atarusov.avitotest.features.apitracks.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

class ApiTrackListViewModel(
    private val repository: TrackRepository
) : ViewModel() {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private val _navigateToPlayer = MutableSharedFlow<Long>()
    val navigateToPlayer: SharedFlow<Long> = _navigateToPlayer

    // not null if error occurred during searching with some query
    private var errorQuery: String? = null

    init {
        loadTracks()
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.ClickOnTrack -> navigateToPlayer(action.trackId)

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

    private fun navigateToPlayer(id: Long) {
        viewModelScope.launch {
            _navigateToPlayer.emit(id)
        }
    }

    private fun loadTracks() {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getChart().catch { error ->
                showError(error)
            }.collect { tracks ->
                showTracks(tracks)
            }
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

    class Factory @Inject constructor(
        private val repository: TrackRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == ApiTrackListViewModel::class.java) {
                return ApiTrackListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}