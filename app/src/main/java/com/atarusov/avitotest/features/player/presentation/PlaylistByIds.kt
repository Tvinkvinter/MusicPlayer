package com.atarusov.avitotest.features.player.presentation

import android.os.Parcelable
import com.atarusov.avitotest.features.player.domain.model.SourceType
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistByIds(
    val currentTrackId: Long,
    val allTrackIds: List<Long>,
    val type: SourceType
): Parcelable
