package com.atarusov.musicplayer.features.player.presentation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.atarusov.musicplayer.App
import com.atarusov.musicplayer.R
import com.atarusov.musicplayer.databinding.FragmentPlayerBinding
import com.atarusov.musicplayer.features.player.presentation.service.PlayerService
import com.atarusov.musicplayer.features.player.presentation.viewmodel.Action
import com.atarusov.musicplayer.features.player.presentation.viewmodel.PlayerViewModel
import com.atarusov.musicplayer.features.player.presentation.viewmodel.State
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val args: PlayerFragmentArgs by navArgs()
    private val playlist: PlaylistByIds get() = args.playlist

    @Inject
    lateinit var factory: PlayerViewModel.Factory
    private val viewModel: PlayerViewModel by viewModels { factory }

    private var playerService: PlayerService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.LocalBinder
            isBound = true
            playerService = binder.getService()
            viewModel.bindService(playerService!!)
            playerService!!.startServiceCommandReceiving(viewModel.serviceCommand)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerService = null
            isBound = false
        }
    }

    var isSeekBarUpdating = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAndRequestPermissionsIfNeeded()

        viewModel.onAction(Action.SetPlaylist(playlist))
        initService()
        setListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    applyState(state)
                }
            }
        }
    }

    private fun checkAndRequestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_POST_NOTIFICATIONS_REQUEST_CODE
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK),
                    PERMISSION_FOREGROUND_SERVICE_REQUEST_CODE
                )
            }
        }
    }

    private fun setListeners() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeekBarUpdating = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.onAction(Action.Seek(seekBar?.progress ?: 0))
                isSeekBarUpdating = false
            }
        })

        binding.btnPlayPause.setOnClickListener {
            viewModel.onAction(Action.PlayPause)
        }

        binding.btnPrev.setOnClickListener {
            viewModel.onAction(Action.Prev)
        }

        binding.btnNext.setOnClickListener {
            viewModel.onAction(Action.Next)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onAction(Action.CloseFragment)
                findNavController().navigateUp()
            }
        })
    }

    private fun applyState(state: State) {
        setTrackOnUI(state)
        updateSeekBar(state)
        updateButtons(state)
    }

    private fun setTrackOnUI(state: State) {
        state.track?.let { track ->
            Glide.with(requireContext())
                .load(track.coverURI)
                .placeholder(R.drawable.ic_default_track_cover_48)
                .error(R.drawable.ic_default_track_cover_48)
                .into(binding.imgCover)

            binding.tvTrackTitle.text = track.trackTitle
            binding.tvAlbumName.text = track.albumName
            binding.tvArtistName.text = track.artistName
        }
    }

    private fun updateSeekBar(state: State) {
        if (!isSeekBarUpdating) {
            binding.seekBar.progress = state.timeElapsed
            binding.seekBar.max = state.timeTotal
        }

        binding.tvTimeElapsed.text = getString(
            R.string.player_fragment_time,
            state.timeElapsed / 60,
            state.timeElapsed % 60
        )
        binding.tvTimeTotal.text = getString(
            R.string.player_fragment_time,
            state.timeTotal / 60,
            state.timeTotal % 60
        )
    }

    private fun updateButtons(state: State) {
        binding.btnPlayPause.setImageResource(
            if (state.isPlaying) R.drawable.ic_pause_36 else R.drawable.ic_play_36
        )

        binding.btnPrev.isEnabled = state.isPrevButtonEnabled
        binding.btnNext.isEnabled = state.isNextButtonEnabled
    }

    private fun initService() {
        val intent = Intent(requireActivity().applicationContext, PlayerService::class.java)
        requireActivity().startForegroundService(intent)
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStart() {
        super.onStart()
        Intent(requireActivity().applicationContext, PlayerService::class.java).also { intent ->
            requireActivity().startForegroundService(intent)
            requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unbindService(serviceConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val PERMISSION_POST_NOTIFICATIONS_REQUEST_CODE = 0
        private const val PERMISSION_FOREGROUND_SERVICE_REQUEST_CODE = 1
    }
}