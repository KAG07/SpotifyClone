package com.example.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide:RequestManager
):BaseSongAdapter(R.layout.list_item){

    override val differ=AsyncListDiffer<Song>(this,diffcallback)

    override fun onBindViewHolder(holder:SongViewholder, position: Int) {
        val song=songs[position]
        holder.itemView.apply {
            tvPrimary.text = song.title
            tvSecondary.text = song.Subtitle
            glide.load(song.imageurl).into(ivItemImage)
            setOnClickListener {
                onItemClickListener?.let {
                    it(song)
                }
            }
        }
    }

}