package com.example.spotifyclone.exoplayer

import android.content.ComponentName
import android.content.Context
import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.spotifyclone.others.Constants.NETWORK_ERROR
import com.example.spotifyclone.others.Event
import com.example.spotifyclone.others.Resource

class MusicServiceConnection(context: Context) {
    private val _isconnected=MutableLiveData<Event<Resource<Boolean>>>()
    val isconnected:LiveData<Event<Resource<Boolean>>> = _isconnected

    private val _networkerror=MutableLiveData<Event<Resource<Boolean>>>()
    val networkerror:LiveData<Event<Resource<Boolean>>> = _networkerror

    private val _playbackstate=MutableLiveData<PlaybackStateCompat?>()
    val playbackstate:LiveData<PlaybackStateCompat?> = _playbackstate

    private val _curplayingsong=MutableLiveData<MediaMetadataCompat?>()
    val curplayingsong:LiveData<MediaMetadataCompat?> = _curplayingsong

    lateinit var mediacontroller:MediaControllerCompat

    private val mediaBowserConnectionCallback=MediaBowserConnectionCallback(context)

    private val mediabrowser=MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBowserConnectionCallback,
        null
    ).apply { connect() }

    val transportcontrols:MediaControllerCompat.TransportControls
    get() = mediacontroller.transportControls//it will initilize when it is used first it's bcs mediacontroller takes time to setup itself

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediabrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediabrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBowserConnectionCallback(private val context: Context):MediaBrowserCompat.ConnectionCallback(){
        override fun onConnected() {
            mediacontroller= MediaControllerCompat(context,mediabrowser.sessionToken).apply {
                registerCallback(MediacontrollerCallback())
            }
            _isconnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            _isconnected.postValue(Event(Resource.error(
                "The connection was suspended", false
            )))
        }

        override fun onConnectionFailed() {
            _isconnected.postValue(Event(Resource.error(
                "Could'nt connect to browser ", false
            )))
        }
    }


    private inner class MediacontrollerCallback():MediaControllerCompat.Callback(){//used to control playback


        override fun onSessionEvent(event: String?, extras: Bundle?) {
           super.onSessionEvent(event, extras)
            when(event){
                NETWORK_ERROR->_networkerror.postValue(
                    Event(
                        Resource.error(
                            "Could Not connect to network.Please check internet connection",
                            null
                        )
                    )
                )
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {//called for song paused or play
           _playbackstate.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {//called when song is changed
            _curplayingsong.postValue(metadata)
        }

        override fun onSessionDestroyed() {
           mediaBowserConnectionCallback.onConnectionSuspended()

        }

    }

}