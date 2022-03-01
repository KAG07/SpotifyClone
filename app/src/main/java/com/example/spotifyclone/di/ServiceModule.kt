package com.example.spotifyclone.di

import android.content.Context
import com.example.spotifyclone.data.remote.MusicDatabase


import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import java.util.jar.Attributes

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {//used in playing music

    @ServiceScoped
    @Provides
    fun providemusicdatabase()=MusicDatabase()

    @ServiceScoped
    @Provides
    fun provideAudioAttributes()= AudioAttributes.Builder()
        .setContentType(CONTENT_TYPE_MUSIC)
        .setUsage(USAGE_MEDIA)
        .build()

    @ServiceScoped
    @Provides
    fun provideexoplayer(
        @ApplicationContext context:Context,
        audioattrb:AudioAttributes//can bet get this from above function
    )=SimpleExoPlayer.Builder(context).build().apply {
        setAudioAttributes(audioattrb,true)
        setHandleAudioBecomingNoisy(true)
    }

    @ServiceScoped
    @Provides
    fun providedatasrcfactory(@ApplicationContext context: Context)=DefaultDataSourceFactory(context,
        Util.getUserAgent(context,"spotify App"))
}