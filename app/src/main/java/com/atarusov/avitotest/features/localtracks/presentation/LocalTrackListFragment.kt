package com.atarusov.avitotest.features.localtracks.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.atarusov.avitotest.App
import com.atarusov.avitotest.R
import com.atarusov.avitotest.base.BaseTrackListFragment
import com.atarusov.avitotest.features.localtracks.presentation.viewmodel.Action
import com.atarusov.avitotest.features.localtracks.presentation.viewmodel.Effect
import com.atarusov.avitotest.features.localtracks.presentation.viewmodel.LocalTrackListViewModel
import com.atarusov.avitotest.features.localtracks.presentation.viewmodel.State
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocalTrackListFragment : BaseTrackListFragment() {

    @Inject
    lateinit var factory: LocalTrackListViewModel.Factory
    private val viewModel: LocalTrackListViewModel by viewModels { factory }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.onAction(Action.ReplyToPermissionRequest(isGranted))
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as App).appComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestAudioPermissionIfNeeded()

        val layoutManager = LinearLayoutManager(context)
        val adapter = LocalTrackAdapter { id ->
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
                    viewModel.state.collectLatest { state ->
                        applyState(state)
                    }
                }

                launch {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            Effect.RequestPermission -> requestAudioPermissionIfNeeded()
                            is Effect.NavigateToPlayer -> TODO()
                        }
                    }
                }
            }
        }
    }

    private fun requestAudioPermissionIfNeeded() {
        val permission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(permission)
        } else {
            viewModel.onAction(Action.ReplyToPermissionRequest(isGranted = true))
        }
    }

    private fun applyState(state: State) {
        binding.searchView.isEnabled = !state.isLoading && !state.isErrorShowing
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        updateRecyclerView(state)
        updateMessage(state)
    }

    private fun updateRecyclerView(state: State) {
        (binding.trackRecyclerView.adapter as LocalTrackAdapter).submitList(state.tracks) {
            binding.trackRecyclerView.scrollToPosition(0)
        }
    }

    private fun updateMessage(state: State) {
        when {
            state.isPermissionDenied -> {
                binding.tvMessage.text =
                    getString(R.string.local_track_list_fragment_permission_denied)
                binding.tvMessage.visibility = View.VISIBLE
                binding.tvRefresh.visibility = View.GONE
            }

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