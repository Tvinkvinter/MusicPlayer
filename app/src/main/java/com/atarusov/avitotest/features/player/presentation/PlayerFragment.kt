package com.atarusov.avitotest.features.player.presentation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.atarusov.avitotest.App
import com.atarusov.avitotest.R
import com.atarusov.avitotest.databinding.FragmentPlayerBinding
import com.atarusov.avitotest.features.player.presentation.service.NotificationAction
import com.atarusov.avitotest.features.player.presentation.service.PlayerNotificationListener
import com.atarusov.avitotest.features.player.presentation.service.PlayerService
import com.atarusov.avitotest.features.player.presentation.viewmodel.Action
import com.atarusov.avitotest.features.player.presentation.viewmodel.PlayerViewModel
import com.atarusov.avitotest.features.player.presentation.viewmodel.ServiceEffect
import com.atarusov.avitotest.features.player.presentation.viewmodel.State
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
            viewModel.onAction(Action.NotifyServiceConnected)
            playerService?.listener = PlayerNotificationListener { action ->
                when (action) {
                    NotificationAction.PLAY_PAUSE -> viewModel.onAction(Action.PlayPause)
                    NotificationAction.PREV -> viewModel.onAction(Action.Prev)
                    NotificationAction.NEXT -> viewModel.onAction(Action.Next)
                    NotificationAction.REWIND_FORWARD -> viewModel.onAction(Action.RewindForward)
                    NotificationAction.REWIND_BACK -> viewModel.onAction(Action.RewindBack)
                }
            }
            viewModel.bindService(playerService!!)
            Log.i("PlayerFragment", "Service bounded by ${this}")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerService = null
            isBound = false
            Log.i("PlayerFragment", "Service UNbounded by ${this}")
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

        setListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collectLatest { state ->
                        applyState(state)
                    }
                }

                launch {
                    viewModel.serviceEffect.collectLatest { effect ->
                        applyEffect(effect)
                    }
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

    private fun applyEffect(effect: ServiceEffect) {
        when (effect) {
            is ServiceEffect.SetTrackAndPlay -> playerService?.setTrackAndPlay(effect.track)
            ServiceEffect.Play -> playerService?.play()
            ServiceEffect.Pause -> playerService?.pause()
            is ServiceEffect.Seek -> playerService?.seek(effect.time)
            is ServiceEffect.Init -> initService()
        }
    }

    private fun initService() {
        Log.i("PlayerFragment", "Service init")
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val PERMISSION_POST_NOTIFICATIONS_REQUEST_CODE = 0
        private const val PERMISSION_FOREGROUND_SERVICE_REQUEST_CODE = 1
    }

}