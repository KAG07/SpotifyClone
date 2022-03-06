package com.example.spotifyclone.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.spotifyclone.R
import com.example.spotifyclone.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)//create all variable singleton
object AppModule {


    @Singleton
    @Provides
    fun providemusicserviceconnection(@ApplicationContext context: Context)=MusicServiceConnection(context)

    @Singleton//same instance used all places
    @Provides
    fun provideglideInstance(@ApplicationContext context: Context)=Glide.with(context).setDefaultRequestOptions(
        RequestOptions().placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )//insert context of application class
}