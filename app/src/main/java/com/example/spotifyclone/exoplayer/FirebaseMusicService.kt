package com.example.spotifyclone.exoplayer

import android.media.MediaMetadata
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.spotifyclone.data.remote.MusicDatabase
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicService @Inject constructor(
    private val musicDatabase: MusicDatabase
) {

    var songs= emptyList<MediaMetadata>()

    suspend fun fetchmetadata()= withContext(Dispatchers.IO){
        state=State.STATE_INITIALISING
        val allsongs=musicDatabase.getAllsongs()
        songs=allsongs.map { song->
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_ARTIST, song.Subtitle)
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, song.imageurl)
                .putString(MediaMetadata.METADATA_KEY_MEDIA_URI, song.songurl)
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, song.imageurl)
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, song.Subtitle)
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, song.Subtitle)
                .build()

        }
        state=State.STATE_INITIALISED
    }

   fun asMediaSource(datasourcefactory:DefaultDataSourceFactory):ConcatenatingMediaSource{
       val concatenatingMediaSOurce=ConcatenatingMediaSource()
       songs.forEach{song->
           val mediasource=ProgressiveMediaSource.Factory(datasourcefactory)
               .createMediaSource(MediaItem.fromUri(song.getString(MediaMetadata.METADATA_KEY_MEDIA_URI).toUri()))
           concatenatingMediaSOurce.addMediaSource(mediasource)
       }
       return concatenatingMediaSOurce
   }

    @RequiresApi(Build.VERSION_CODES.O)
    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(MediaMetadata.METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()


    private val mreadylisteners= mutableListOf<(Boolean)->Unit>()//list of lambda func that take sboolean to schedule actions when music is downloaded
    private var state =State.STATE_CREATED
    set(value){//setter
        if(value==State.STATE_INITIALISED||value==State.STATE_ERROR){
            synchronized(mreadylisteners){//only one thread can access readylisteners
                field=value//assign new state to state variable
                mreadylisteners.forEach {listen->
                    listen(state==State.STATE_INITIALISED)
                }
            }
        }else{
            field=value
        }
    }

    fun whenready(action:(Boolean)->Unit):Boolean{//checks music is ready or not
        if(state == State.STATE_CREATED || state == State.STATE_INITIALISING) {
            mreadylisteners += action//when music is ready then it will be executed
            return false
        } else {
            action(state == State.STATE_INITIALISED)
            return true
        }
    }
}

enum class State{//music source can be in following state
    STATE_CREATED,
    STATE_INITIALISING,
    STATE_INITIALISED,
    STATE_ERROR
}