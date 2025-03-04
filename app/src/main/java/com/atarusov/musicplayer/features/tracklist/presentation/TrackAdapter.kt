package com.atarusov.musicplayer.features.tracklist.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atarusov.musicplayer.R
import com.atarusov.musicplayer.databinding.TrackItemBinding
import com.atarusov.musicplayer.features.tracklist.domain.model.Track
import com.bumptech.glide.Glide

class TrackAdapter(
    val onClickAction: (id: Long) -> Unit
) : ListAdapter<Track, TrackAdapter.TrackViewHolder>(TrackDiffCallback()) {

    inner class TrackViewHolder(private val binding: TrackItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(track: Track) {
            Glide.with(itemView)
                .load(track.coverURI)
                .error(R.drawable.ic_default_track_cover_48)
                .into(binding.imgCover)

            binding.tvTrackTitle.text = track.trackTitle
            binding.tvArtistName.text = track.artistName

            itemView.setOnClickListener {
                onClickAction.invoke(track.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)
    }
}

class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
        return oldItem == newItem
    }
}