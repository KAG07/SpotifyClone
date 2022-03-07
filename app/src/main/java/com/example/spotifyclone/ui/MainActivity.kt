package com.example.spotifyclone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.adapters.SwipeSongAdapter
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.exoplayer.isPlaying
import com.example.spotifyclone.exoplayer.toSong
import com.example.spotifyclone.others.Status
import com.example.spotifyclone.ui.Mainviewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainviewmodel :MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var curplayinsong:Song?=null

    private var playbackstate:PlaybackStateCompat?=null

    @Inject
    lateinit var glide: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribetoobservers()

        vpSong.adapter=swipeSongAdapter

        vpSong.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackstate?.isPlaying==true){
                    mainviewmodel.playortogglesong(swipeSongAdapter.songs[position])
                }
                else{
                    curplayinsong=swipeSongAdapter.songs[position]
                }
            }
        })

        ivPlayPause.setOnClickListener {
            curplayinsong?.let {
                mainviewmodel.playortogglesong(it,true)
            }
        }
        swipeSongAdapter.setItemClickListener {
            navHostFragment.findNavController().navigate(
                R.id.globaltosongfragment
            )
        }

        navHostFragment.findNavController().addOnDestinationChangedListener{_,dest,_->
            when(dest.id){
                R.id.songfragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }
    }

    private fun showBottomBar() {
        ivCurSongImage.isVisible = true
        vpSong.isVisible = true
        ivPlayPause.isVisible = true
    }

    private fun hideBottomBar() {
        ivCurSongImage.isVisible = false
        vpSong.isVisible = false
        ivPlayPause.isVisible = false
    }

    private fun swithviewpagertocurrsong(song:Song){
        var ind=swipeSongAdapter.songs.indexOf(song)

        if(ind!=-1){
            vpSong.currentItem=ind
            curplayinsong=song
        }
    }

    private fun subscribetoobservers() {
        mainviewmodel.mediaitem.observe(this){
            it?.let {result->
                when(result.status){
                    Status.SUCCESS->{
                        result.data?.let {
                            swipeSongAdapter.songs=it
                            if(it.isNotEmpty()) {
                                glide.load((curplayinsong ?: it[0]).imageurl).into(ivCurSongImage)
                            }
                            swithviewpagertocurrsong(curplayinsong?:return@observe)
                        }
                    }
                    Status.ERROR->Unit
                    Status.LOADING->Unit
                }
            }
        }
        mainviewmodel.curPlayingSong.observe(this){
            if(it==null)return@observe

            curplayinsong=it.toSong()
            glide.load(curplayinsong?.imageurl).into(ivCurSongImage)
            swithviewpagertocurrsong(curplayinsong?:return@observe)
        }

        mainviewmodel.playbackState.observe(this){
            playbackstate=it
            ivPlayPause.setImageResource(
                if(playbackstate?.isPlaying==true)R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        mainviewmodel.isconnected.observe(this){
            it?.getcontentifnothandled()?.let {result->
                when(result.status){
                    Status.ERROR->Snackbar.make(
                        rootLayout,
                        result.msg ?: "An unknown error occured",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else->Unit
                }
            }
        }

        mainviewmodel.networkError.observe(this){
            it?.getcontentifnothandled()?.let {result->
                when(result.status){
                    Status.ERROR->Snackbar.make(
                        rootLayout,
                        result.msg ?: "An unknown error occured",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else->Unit
                }
            }
        }
    }
}