package com.atarusov.avitotest.features.player.data.local

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.atarusov.avitotest.features.player.domain.model.Track
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TrackLocalDataSource @Inject constructor() {
    @Inject
    lateinit var contentResolver: ContentResolver

    fun getLocalTrackById(trackId: Long) = flow {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
        )
        val selection = "${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(trackId.toString())

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )

        val coverBaseURI = Uri.parse("content://media/external/audio/albumart")

        cursor?.use {
            val idColumn = it.getColumnIndex(MediaStore.Audio.Media._ID)
            val albumIdColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val albumNameColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val titleColumn = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val trackURIColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)

            val albumName = if (albumNameColumn != -1) it.getString(albumNameColumn) else null

            while (it.moveToNext()) {
                val albumId = it.getLong(albumIdColumn)

                val track = Track(
                    id = it.getLong(idColumn),
                    coverURI = ContentUris.withAppendedId(coverBaseURI, albumId).toString(),
                    trackTitle = it.getString(titleColumn),
                    albumName = albumName,
                    artistName = it.getString(artistColumn),
                    trackURI = it.getString(trackURIColumn),
                )

                emit(track)
            }
        }
    }
}
