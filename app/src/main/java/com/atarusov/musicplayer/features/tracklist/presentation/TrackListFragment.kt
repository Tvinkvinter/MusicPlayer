package com.atarusov.musicplayer.features.tracklist.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.atarusov.musicplayer.App
import com.atarusov.musicplayer.R
import com.atarusov.musicplayer.databinding.FragmentTrackListBinding
import com.atarusov.musicplayer.features.tracklist.presentation.viewmodel.Action
import com.atarusov.musicplayer.features.tracklist.presentation.viewmodel.State
import com.atarusov.musicplayer.features.tracklist.presentation.viewmodel.TrackListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class TrackListFragment : Fragment() {

    private var _binding: FragmentTrackListBinding? = null
    private val binding get() = _binding!!

    private val args: TrackListFragmentArgs by navArgs()
    private val sourceType by lazy { args.sourceType }

    @Inject
    lateinit var factory: TrackListViewModel.ViewModelFactory.FactoryProvider

    private val viewModel: TrackListViewModel by viewModels {
        factory.create(sourceType)
    }

    override fun onAttach(context: Context) {
        (requireActivity().application as App).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        val adapter = TrackAdapter { id ->
            viewModel.onAction(Action.ClickOnTrack(id))
        }
        binding.trackRecyclerView.layoutManager = layoutManager
        binding.trackRecyclerView.adapter = adapter

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
                            TrackListFragmentDirections.actionTrackListFragmentToPlayerFragment(
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
        (binding.trackRecyclerView.adapter as TrackAdapter).submitList(state.tracks) {
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}