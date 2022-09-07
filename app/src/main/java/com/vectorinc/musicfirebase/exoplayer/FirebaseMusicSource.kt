package com.vectorinc.musicfirebase.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.vectorinc.musicfirebase.data.remote.MusicDatabase
import com.vectorinc.musicfirebase.exoplayer.State.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource  @Inject constructor(
    private val musicDatabase: MusicDatabase
){

    var songs = emptyList<MediaMetadataCompat>()
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit >()
    private var state : State = STATE_CREATED
    set(value) {
        if(value == STATE_INITIALIZED || value == STATE_ERROR){
           synchronized(onReadyListeners){
               field = value
               onReadyListeners.forEach{listeners->

                   listeners(state == STATE_INITIALIZED)
               }
           }
        }else{
            field = value
        }
    }

    suspend fun fetchMediaData() = withContext(Dispatchers.IO){
        state = STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()
        songs = allSongs.map {song->

            Builder()
                .putString(METADATA_KEY_ARTIST,song.subTitle)
                .putString(METADATA_KEY_MEDIA_ID,song.mediaId)
                .putString(METADATA_KEY_MEDIA_URI,song.songUrl)
                .putString(METADATA_KEY_TITLE,song.title)
                .putString(METADATA_KEY_AUTHOR,song.subTitle)
                .putString(METADATA_KEY_DISPLAY_TITLE,song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI,song.imageUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI,song.imageUrl)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION,song.subTitle)
                .build()
        }
        state = STATE_INITIALIZED

    }
    fun asMediaItems() = songs.map{song->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc,FLAG_PLAYABLE)

    }

    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource{
       val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song->
           val mediaItem = MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
        }
        return concatenatingMediaSource
    }

    fun whenReady(action : (Boolean) -> Unit): Boolean{
        if (state == STATE_CREATED || state == STATE_INITIALIZING){
            onReadyListeners += action
            return false
        }else{
            action(state==STATE_INITIALIZED)
            return true
        }

    }
}

enum class State {
    STATE_INITIALIZED,
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_ERROR
}
