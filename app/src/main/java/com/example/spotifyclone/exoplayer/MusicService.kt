package com.example.spotifyclone.exoplayer

import android.app.PendingIntent
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.spotifyclone.exoplayer.callbacks.MusicPlayerNotificationListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject

private const val SERVICE_TAG="MusicService"

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val servicejob= Job()
    private val servicescope= CoroutineScope(Dispatchers.Main+servicejob)

    private lateinit var mediasession:MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    override fun onCreate() {
        super.onCreate()
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
        ){}

        mediaSessionConnector= MediaSessionConnector(mediasession)
        mediaSessionConnector.setPlayer(exoPlayer)

    }

    override fun onDestroy() {
        super.onDestroy()
        servicescope.cancel()
    }

    override fun onGetRoot(p0: String, p1: Int, p2: Bundle?): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        TODO("Not yet implemented")
    }

}