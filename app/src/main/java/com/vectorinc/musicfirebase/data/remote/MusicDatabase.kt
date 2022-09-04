package com.vectorinc.musicfirebase.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.vectorinc.musicfirebase.data.remote.dto.SongDto
import com.vectorinc.musicfirebase.utils.Constants.SONG_COLLECTIONS
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val songCollections = firestore.collection(SONG_COLLECTIONS)

    suspend fun getAllSongs(): List<SongDto> {
        return try {
            songCollections.get().await().toObjects(SongDto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

}