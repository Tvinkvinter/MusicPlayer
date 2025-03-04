package com.atarusov.musicplayer.features.tracklist.data.local

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.atarusov.musicplayer.features.tracklist.domain.model.Track
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TrackLocalDataSource @Inject constructor(private val contentResolver: ContentResolver) {

    fun getLocalTracks(query: String? = null) = flow {
        val tracks = mutableListOf<Track>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
        )
        var selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val selectionArgs = mutableListOf<String>()

        if (!query.isNullOrBlank()) {
            selection += " AND (${MediaStore.Audio.Media.TITLE} LIKE ? OR ${MediaStore.Audio.Media.ARTIST} LIKE ?)"
            selectionArgs.addAll(listOf("%$query%", "%$query%"))
        }

        val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs.toTypedArray(),
            sortOrder
        )

        val coverBaseURI = Uri.parse("content://media/external/audio/albumart")

        cursor?.use {
            val idColumn = it.getColumnIndex(MediaStore.Audio.Media._ID)
            val albumIdColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val titleColumn = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val trackTitle = it.getString(titleColumn)
                val artistName = it.getString(artistColumn)

                val albumId = it.getLong(albumIdColumn)
                val coverURI = ContentUris.withAppendedId(coverBaseURI, albumId).toString()

                tracks.add(
                    Track(
                        id = id,
                        coverURI = coverURI,
                        trackTitle = trackTitle,
                        artistName = artistName
                    )
                )
            }
        }
        emit(tracks.toList())
    }
}