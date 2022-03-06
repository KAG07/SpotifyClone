package com.example.spotifyclone.data.remote

import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.others.Constants.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val firestore=FirebaseFirestore.getInstance()
    private val songcollection=firestore.collection(SONG_COLLECTION)

    suspend fun getAllsongs():List<Song>{
        return  try{
            songcollection.get().await().toObjects(Song::class.java)//await give of type any we converted into song
        }catch (e:Exception){
            emptyList()
        }
    }

}