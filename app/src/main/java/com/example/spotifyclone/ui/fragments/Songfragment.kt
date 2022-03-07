package com.example.spotifyclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.exoplayer.isPlaying
import com.example.spotifyclone.exoplayer.toSong
import com.example.spotifyclone.others.Status
import com.example.spotifyclone.ui.Mainviewmodel.MainViewModel
import com.example.spotifyclone.ui.Mainviewmodel.SongViewmodel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class Songfragment:Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var  glide:RequestManager

    private lateinit var mainViewModel: MainViewModel

    private val songViewmodel:SongViewmodel by viewModels()

    private var curplayingSong: Song?=null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()

        ivPlayPauseDetail.setOnClickListener {
            curplayingSong?.let {
                mainViewModel.playortogglesong(it,true)
            }
        }

        seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2) {
                    setCurPlayerTimeToTextView(p1.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                shouldUpdateSeekbar=false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let {
                    mainViewModel.seekto(p0.progress.toLong())
                    shouldUpdateSeekbar=true
                }
            }
        })

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skiptoprvssong()
        }

        ivSkip.setOnClickListener {
            mainViewModel.skiptonextsong()
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaitem.observe(viewLifecycleOwner) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if(curplayingSong == null && songs.isNotEmpty()) {
                                curplayingSong = songs[0]
                                Updatetitleandimage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            if(it == null) return@observe
            curplayingSong = it.toSong()
            Updatetitleandimage(curplayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playbackState=it
            ivPlayPauseDetail.setImageResource(
                if(playbackState?.isPlaying==true)R.drawable.ic_pause else R.drawable.ic_play
            )
            seekBar.progress=it?.position?.toInt()?:0
        }

        songViewmodel.curPlayerPosition.observe(viewLifecycleOwner) {
            if(shouldUpdateSeekbar) {
                seekBar.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }
        songViewmodel.curSongDuration.observe(viewLifecycleOwner) {
            seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it)
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long?) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms)
    }

    private fun Updatetitleandimage(song:Song){
        val title = "${song.title} - ${song.Subtitle}"
        tvSongName.text = title
        glide.load(song.imageurl).into(ivSongImage)
    }


}