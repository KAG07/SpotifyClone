package com.example.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.media.MediaBrowserServiceCompat
import com.example.spotifyclone.exoplayer.callbacks.MusicPlayerEventListener
import com.example.spotifyclone.exoplayer.callbacks.MusicPlayerNotificationListener
import com.example.spotifyclone.exoplayer.callbacks.MusicPlayerPreparer
import com.example.spotifyclone.others.Constants.MEDIA_ROOT_ID
import com.example.spotifyclone.others.Constants.NETWORK_ERROR
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


private const val SERVICE_TAG="MusicService"

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    var mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    private lateinit var musicNotificationManager: MusicNotificationManager

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicService

    private val serviceJob = Job()
    private val servicescope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val servicescope1 = CoroutineScope(Dispatchers.Main )
    private lateinit var mediasession:MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private var curPlayingSong: MediaMetadataCompat? = null


    private var isPlayerInitialized = false

    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    companion object{
         var cursongdur=0L
        private set
    }



    override fun onCreate() {
        super.onCreate()

        servicescope.launch {
            firebaseMusicSource.fetchmetadata()
        }

        val activityintent=packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,0)
        }
        mediasession=MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityintent)
            isActive=true
        }
        sessionToken=mediasession.sessionToken

        musicNotificationManager= MusicNotificationManager(
            this,
            mediasession.sessionToken,
            MusicPlayerNotificationListener(this)
        ){
            cursongdur=exoPlayer.duration
        }

        val musicplaybackpreparer=MusicPlayerPreparer(firebaseMusicSource){
            curPlayingSong=it
            prepareplayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }

        mediaSessionConnector= MediaSessionConnector(mediasession)
        mediaSessionConnector.setPlaybackPreparer(musicplaybackpreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener= MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.shownotification(exoPlayer)

    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediasession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

    private fun prepareplayer(
        songs: List<MediaMetadataCompat>,
        itemtoplay: MediaMetadataCompat?,
        playnow: Boolean,
    ){
        val curSongIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemtoplay)
        servicescope1.launch {
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playnow
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        servicescope.cancel()
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(p0: String, p1: Int, p2: Bundle?): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID,null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        when(parentId){
            MEDIA_ROOT_ID->{
                val resultsSent = firebaseMusicSource.whenready { isInitialized ->
                    if(isInitialized) {
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        if(!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                            prepareplayer(firebaseMusicSource.songs, firebaseMusicSource.songs[0], false)
                            isPlayerInitialized = true
                        }
                    } else {
                        mediasession.sendSessionEvent(NETWORK_ERROR,null)
                        result.sendResult(null)
                    }
                }
                if(!resultsSent) {
                    result.detach()
                }
            }
        }
    }

}