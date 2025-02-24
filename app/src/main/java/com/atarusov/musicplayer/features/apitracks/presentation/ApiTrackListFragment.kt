package com.atarusov.musicplayer.features.apitracks.presentation

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.atarusov.musicplayer.App
import com.atarusov.musicplayer.R
import com.atarusov.musicplayer.base.BaseTrackListFragment
import com.atarusov.musicplayer.features.apitracks.presentation.viewmodel.Action
import com.atarusov.musicplayer.features.apitracks.presentation.viewmodel.ApiTrackListViewModel
import com.atarusov.musicplayer.features.apitracks.presentation.viewmodel.State
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class ApiTrackListFragment : BaseTrackListFragment() {

    @Inject
    lateinit var factory: ApiTrackListViewModel.Factory
    private val viewModel: ApiTrackListViewModel by viewModels { factory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as App).appComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        val adapter = ApiTrackAdapter { id ->
            viewModel.onAction(Action.ClickOnTrack(id))
        }
        binding.trackRecyclerView.layoutManager = layoutManager
        binding.trackRecyclerView.adapter = adapter

        //TODO(): clear focus on submit button click when SearchView is empty
        binding.searchView.isSubmitButtonEnabled = true
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onAction(Action.SearchTrack(newText))
                return true
            }
        })

        binding.tvRefresh.setOnClickListener {
            viewModel.onAction(Action.RepeatRequest)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        applyState(state)
                    }
                }

                launch {
                    viewModel.navigateToPlayer.collectLatest { playlist ->
                        findNavController().navigate(
                            ApiTrackListFragmentDirections.actionApiTrackListFragmentToPlayerFragment(
                                playlist
                            )
                        )
                    }
                }
            }
        }
    }

    private fun applyState(state: State) {
        binding.searchView.isEnabled = !state.isLoading && !state.isErrorShowing
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        updateRecyclerView(state)
        updateMessage(state)
    }

    private fun updateRecyclerView(state: State) {
        (binding.trackRecyclerView.adapter as ApiTrackAdapter).submitList(state.tracks) {
            binding.trackRecyclerView.scrollToPosition(0)
        }
    }

    private fun updateMessage(state: State) {
        when {
            state.isErrorShowing -> {
                binding.tvMessage.text = getString(R.string.base_track_list_fragment_unknown_error)
                binding.tvMessage.visibility = View.VISIBLE
                binding.tvRefresh.visibility = View.VISIBLE
            }

            state.noTracks -> {
                binding.tvMessage.text = getString(R.string.base_track_list_fragment_no_tracks)
                binding.tvMessage.visibility = View.VISIBLE
                binding.tvRefresh.visibility = View.GONE
            }

            else -> {
                binding.tvMessage.visibility = View.GONE
                binding.tvRefresh.visibility = View.GONE
            }
        }
    }
}