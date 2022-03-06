package com.example.spotifyclone.ui.Mainviewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.others.Constants.MEDIA_ROOT_ID
import com.example.spotifyclone.others.Resource
import com.example.spotifyclone.exoplayer.MusicServiceConnection
import com.example.spotifyclone.exoplayer.isPlayEnabled
import com.example.spotifyclone.exoplayer.isPlaying
import com.example.spotifyclone.exoplayer.isPrepared
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val musicServiceConnection: MusicServiceConnection):ViewModel() {
    private val _mediaItems=MutableLiveData<Resource<List<Song>>>()
    val mediaitem:LiveData<Resource<List<Song>>> = _mediaItems

    val isconnected = musicServiceConnection.isconnected
    val networkError = musicServiceConnection.networkerror
    val curPlayingSong = musicServiceConnection.curplayingsong
    val playbackState = musicServiceConnection.playbackstate

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID,object :MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                var items=children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skiptonextsong(){
        musicServiceConnection.transportcontrols.skipToNext()
    }
    fun skiptoprvssong(){
        musicServiceConnection.transportcontrols.skipToPrevious()
    }
    fun seekto(pos:Long){
        musicServiceConnection.transportcontrols.seekTo(pos)
    }


    fun playortogglesong(mediaItemSong:Song,toggle:Boolean=false){//to pause and play some other song
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItemSong.mediaId ==
            curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportcontrols.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportcontrols.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportcontrols.playFromMediaId(mediaItemSong.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,object :MediaBrowserCompat.SubscriptionCallback(){})
    }
}